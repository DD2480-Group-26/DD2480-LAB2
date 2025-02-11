package com;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

public class TestGithubWebhookIntegrationTest {

    private Server server;
    private int port;

    // Helper method to create and start a new server with the provided servlet.
    private void startServerWithServlet(GithubWebhook servlet) throws Exception {
        server = new Server(0); // use ephemeral port
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(servlet), "/webhook");
        server.setHandler(handler);
        server.start();
        port = server.getURI().getPort();
        System.out.println("Server started on port: " + port);
    }

    @AfterEach
    public void stopServer() throws Exception {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    // Helper method to send a POST request to the /webhook endpoint.
    private String sendPostRequest(String jsonPayload) throws Exception {
        URI uri = new URI("http", null, "localhost", port, "/webhook", null, null);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        connection.getOutputStream().write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        connection.getOutputStream().flush();
        connection.getOutputStream().close();

        int responseCode = connection.getResponseCode();
        // Use getErrorStream() if response code indicates an error (>= 400)
        InputStream is = (responseCode >= 400) ? connection.getErrorStream() : connection.getInputStream();
        String responseBody = new BufferedReader(new InputStreamReader(is))
            .lines().collect(Collectors.joining("\n"));
        return responseCode + ":" + responseBody;
    }

    @Test
    public void testDoPostIntegrationSuccess() throws Exception {
        // Use TestGithubWebhook with both phases simulating success.
        TestGithubWebhook servlet = new TestGithubWebhook();
        servlet.setSimulateCompileFailure(false);
        servlet.setSimulateTestFailure(false);

        // Create and start the server with this servlet.
        startServerWithServlet(servlet);

        String jsonPayload = "{"
                + "\"after\": \"abc123\","
                + "\"repository\": {"
                + "    \"name\": \"test-repo\","
                + "    \"owner\": {\"login\": \"test-user\"}"
                + "},"
                + "\"ref\": \"refs/heads/main\""
                + "}";
        String result = sendPostRequest(jsonPayload);
        String[] parts = result.split(":", 2);
        int responseCode = Integer.parseInt(parts[0]);
        String responseBody = parts[1];

        System.out.println("Success test response code: " + responseCode);
        System.out.println("Success test response body: " + responseBody);

        assertEquals(200, responseCode, "Expected HTTP 200 for success.");
        assertTrue(responseBody.contains("Build succeeded."), "Expected 'Build succeeded.' in response.");
    }

    @Test
    public void testDoPostIntegrationCompileFailure() throws Exception {
        // Use TestGithubWebhook that simulates a compile phase failure.
        TestGithubWebhook servlet = new TestGithubWebhook();
        servlet.setSimulateCompileFailure(true);
        servlet.setSimulateTestFailure(false);

        // Create and start the server with this servlet.
        startServerWithServlet(servlet);

        String jsonPayload = "{"
                + "\"after\": \"abc123\","
                + "\"repository\": {"
                + "    \"name\": \"test-repo\","
                + "    \"owner\": {\"login\": \"test-user\"}"
                + "},"
                + "\"ref\": \"refs/heads/main\""
                + "}";
        String result = sendPostRequest(jsonPayload);
        String[] parts = result.split(":", 2);
        int responseCode = Integer.parseInt(parts[0]);
        String responseBody = parts[1];

        System.out.println("Compile failure test response code: " + responseCode);
        System.out.println("Compile failure test response body: " + responseBody);

        assertEquals(500, responseCode, "Expected HTTP 500 for compile phase failure.");
        assertTrue(responseBody.contains("Build failed."), "Expected 'Build failed.' in response.");
    }

    @Test
    public void testDoPostIntegrationTestFailure() throws Exception {
        // Use TestGithubWebhook that simulates a test phase failure.
        TestGithubWebhook servlet = new TestGithubWebhook();
        servlet.setSimulateCompileFailure(false);
        servlet.setSimulateTestFailure(true);

        // Create and start the server with this servlet.
        startServerWithServlet(servlet);

        String jsonPayload = "{"
                + "\"after\": \"abc123\","
                + "\"repository\": {"
                + "    \"name\": \"test-repo\","
                + "    \"owner\": {\"login\": \"test-user\"}"
                + "},"
                + "\"ref\": \"refs/heads/main\""
                + "}";
        String result = sendPostRequest(jsonPayload);
        String[] parts = result.split(":", 2);
        int responseCode = Integer.parseInt(parts[0]);
        String responseBody = parts[1];

        System.out.println("Test failure test response code: " + responseCode);
        System.out.println("Test failure test response body: " + responseBody);

        assertEquals(500, responseCode, "Expected HTTP 500 for test phase failure.");
        assertTrue(responseBody.contains("Build failed."), "Expected 'Build failed.' in response.");
    }
}
