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
}