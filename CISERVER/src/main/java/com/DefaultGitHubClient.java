package com;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class DefaultGitHubClient implements GitHubClient {
    private final String baseUrl;

    public DefaultGitHubClient() {
        this("https://api.github.com"); // Default to GitHub API
    }

    public DefaultGitHubClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void postStatus(String owner, String repo, String commitSHA, String state) {

        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Missing GITHUB_TOKEN environment variable");
        }


        String url = String.format("%s/repos/%s/%s/statuses/%s", baseUrl, owner, repo, commitSHA);
        String description = getDescription(state);
        String context = "continuous-integration/jetty";
        String jsonBody = String.format(
            "{\"state\": \"%s\", \"context\": \"%s\", \"description\": \"%s\"}",
            state, context, description
            );
            
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "CI-Server")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("Failed to post status. Response code: " + response.statusCode() + ", body: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while sending status to GitHub", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send status to GitHub", e);
        }
    }

    public String getDescription(String state) {
        switch (state.toLowerCase()) {
            case "success":
                return "Build succeeded";
            case "failure":
                return "Build failed";
            case "error":
                return "Build encountered an error";
            case "pending":
                return "Build is pending";
            default:
                return "Build status: " + state;
        }
    }
}