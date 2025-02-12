package com;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


/*
 * Class for a servlet for handling webhook from github.s
 * 
 */
public class GithubWebhook extends HttpServlet {

    // GitHubClient implementation
    private GitHubClient gitHubClient;

    /*
     * A sample response for the browser. 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println("This servlet is up and running.");
    }


    /*
     * The webhook is handled here and the 
     * 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Read the webhook payload from the request body
            String payload;
            try (InputStream inputStream = request.getInputStream()) {
                payload = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Default values if extraction fails
            String repoName = "unknown";
            String commitSHA = "unknown";
            String branch = "unknown";
            String ownerLogin = "unknown";
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(payload);
                commitSHA = root.path("after").asText();
                JsonNode repoNode = root.path("repository");
                repoName = repoNode.path("name").asText();
                ownerLogin = repoNode.path("owner").path("login").asText();
                branch = root.path("ref").asText();

                System.out.println("Owner Login: " + ownerLogin);
                System.out.println("Repository: " + repoName);
                System.out.println("Commit SHA (after): " + commitSHA);
                System.out.println("Branch: " + branch);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            int exitCode = 0;
            String buildErrorDetails = "";
            try {
                runBuildAtCommit(ownerLogin, repoName, commitSHA);

            } catch (Exception e) {
                buildErrorDetails = e.getMessage();
                exitCode = 1;
            }

            // Prepare build status information.
            boolean success = exitCode == 0;
            String statusString = success ? "success" : "failure";
            String details = success ? "Build succeeded." : "Build and/or tests failed: " + buildErrorDetails;

            // Create a BuildStatus object and save it.
            BuildStatus buildStatus = new BuildStatus(repoName, commitSHA, branch, success, details);
            FileBuildStatusStore.addStatus(buildStatus);

            System.out.println("Posting to GitHub.");
            gitHubClient.postStatus(ownerLogin, repoName, commitSHA, statusString);


            // Respond based on the build result
            if (exitCode == 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("Build succeeded.");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Build failed.");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error handling webhook: " + e.getMessage());
        }
    }

    /*
     * Function for cloning the repo and checking out the based on the listed SHA. 
     * 
     */
    protected void runBuildAtCommit(String owner, String repo, String commitSHA) throws Exception {
        String token = System.getenv("GITHUB_TOKEN");
        if (token == null) {
            throw new Exception("GITHUB_TOKEN environment variable is not set.");
         }
        String cloneUrl = "https://" + token + "@github.com/" + owner + "/" + repo + ".git";
        File workspace = new File("workspace_" + System.currentTimeMillis());
        
        workspace.mkdirs();
        System.out.println(workspace.getAbsolutePath());
        // Clone the repository into the workspace.
        ProcessBuilder clonePB = new ProcessBuilder("git", "clone", cloneUrl, workspace.getAbsolutePath());
        clonePB.redirectErrorStream(true);
        Process cloneProcess = clonePB.start();
        String cloneOutput = readProcessOutput(cloneProcess);
        int cloneExit = cloneProcess.waitFor();
        if (cloneExit != 0) {
            throw new Exception("Git clone failed");
        }
        // Check out the specific commit.
        ProcessBuilder checkoutPB = new ProcessBuilder("git", "checkout", commitSHA);
        checkoutPB.directory(workspace);
        checkoutPB.redirectErrorStream(true);
        Process checkoutProcess = checkoutPB.start();
        String checkoutOutput = readProcessOutput(checkoutProcess);
        int checkoutExit = checkoutProcess.waitFor();
        if (checkoutExit != 0) {
            throw new Exception("Git checkout of commit " + commitSHA + " failed: " + checkoutOutput);
        }
        // Run the Maven build (compile and test) in the workspace.
        runCompilePhase(workspace);
        runTestPhase(workspace);


    }

    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
    
    /**
     * Runs the Maven compile phase using ProcessBuilder.
     * Executes "mvn -B clean compile" in the current directory.
     * If the process fails, throws an Exception including the full log output.
     */
    protected void runCompilePhase(File workspace) throws Exception {
        System.out.println("Starting compile phase...");
        ProcessBuilder pb = new ProcessBuilder("mvn", "-B", "clean", "compile");
        pb.directory(workspace); // Directory containing your pom.xml
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder compileOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                compileOutput.append(line).append("\n");
            }
        }
        int exitCode = process.waitFor();
        System.out.println("Compile phase exit code: " + exitCode);
        System.out.println("Compile output:\n" + compileOutput.toString());
        if (exitCode != 0) {
            throw new Exception("Compilation failed with exit code " + exitCode + "\n" + compileOutput.toString());
        }
    }


    /**
     * Runs the Maven test phase using ProcessBuilder.
     * Executes "mvn " in the current directory.
     * If the process fails, throws an Exception including the full log output.
     */
    protected void runTestPhase(File workspace) throws Exception {
        System.out.println("Starting test phase...");
        ProcessBuilder pb = new ProcessBuilder("mvn", "test");
        pb.directory(workspace); 
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder testOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                testOutput.append(line).append("\n");
            }
        }
        int exitCode = process.waitFor();
        System.out.println("Test phase exit code: " + exitCode);
        System.out.println("Test output:\n" + testOutput.toString());
        if (exitCode != 0) {
            throw new Exception("Test phase failed with exit code " + exitCode + "\n" + testOutput.toString());
        }
    }
}


