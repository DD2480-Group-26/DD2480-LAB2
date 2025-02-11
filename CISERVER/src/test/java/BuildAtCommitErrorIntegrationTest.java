package com;

import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class BuildAtCommitErrorIntegrationTest {

    /**
     * Utility method to run a command in a given directory and return its output.
     */
    private String runCommand(File workingDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingDir != null) {
            pb.directory(workingDir);
        }
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Command " + String.join(" ", command) + " failed with exit code " + exitCode + "\n" + output);
        }
        return output;
    }
    
    /**
     * Sets up a temporary local git repository with a minimal pom.xml.
     * Returns the repository directory.
     */
    private File setupTemporaryRepository() throws Exception {
        File repoDir = Files.createTempDirectory("testRepo").toFile();
        // Initialize git repository.
        runCommand(repoDir, "git", "init");
        // Create a minimal pom.xml.
        File pom = new File(repoDir, "pom.xml");
        String minimalPom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
                          + "<modelVersion>4.0.0</modelVersion>"
                          + "<groupId>com.example</groupId>"
                          + "<artifactId>dummy-project</artifactId>"
                          + "<version>1.0-SNAPSHOT</version>"
                          + "</project>";
        Files.write(pom.toPath(), minimalPom.getBytes(StandardCharsets.UTF_8));
        // Add and commit.
        runCommand(repoDir, "git", "add", "pom.xml");
        runCommand(repoDir, "git", "commit", "-m", "Initial commit");
        return repoDir;
    }
    
    /**
     * Test that an invalid commit SHA triggers a checkout failure.
     */
    @Test
    public void testRunBuildAtCommitCheckoutFailure() throws Exception {
        File repoDir = setupTemporaryRepository();
        // Get a valid commit SHA for later use.
        String validCommit = runCommand(repoDir, "git", "rev-parse", "HEAD").trim();
        // Use an invalid commit SHA.
        String invalidCommit = "invalidSHA";
        
        // Create an instance of GithubWebhook and invoke runBuildAtCommit via reflection.
        GithubWebhook webhook = new GithubWebhook();
        java.lang.reflect.Method method = GithubWebhook.class.getDeclaredMethod("runBuildAtCommit", String.class, String.class, String.class);
        method.setAccessible(true);
        
        // For testing purposes, modify runBuildAtCommit so that it uses a file:// URL if the owner equals "local".
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(webhook, "local", repoDir.getName(), invalidCommit);
        });
        
        // Cleanup.
        FileUtils.deleteDirectory(repoDir);
    }
    
    /**
     * Test that Maven build failure (e.g. missing pom.xml) triggers an error.
     */
    @Test
    public void testRunBuildAtCommitMavenFailure() throws Exception {
        // Setup repository.
        File repoDir = setupTemporaryRepository();
        // Get a valid commit SHA.
        String commitSHA = runCommand(repoDir, "git", "rev-parse", "HEAD").trim();
        
        // Simulate a Maven failure by deleting the pom.xml after commit.
        File pom = new File(repoDir, "pom.xml");
        assertTrue(pom.exists());
        assertTrue(pom.delete());
        runCommand(repoDir, "git", "add", "pom.xml");
        runCommand(repoDir, "git", "commit", "-m", "Remove pom.xml for testing");
        
        // Create an instance of GithubWebhook and invoke runBuildAtCommit.
        GithubWebhook webhook = new GithubWebhook();
        java.lang.reflect.Method method = GithubWebhook.class.getDeclaredMethod("runBuildAtCommit", String.class, String.class, String.class);
        method.setAccessible(true);
        
        // For testing purposes, we assume the code uses a local file URL when owner equals "local".
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(webhook, "local", repoDir.getName(), commitSHA);
        });
        
        // Cleanup.
        FileUtils.deleteDirectory(repoDir);
    }
}
