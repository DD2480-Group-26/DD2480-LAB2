package com;

import com.CIServer;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.stream.Collectors;


/*
 *Code for testing the CIserver itself works as expected. 
 * 
 */
public class CIServerTest {

    private Server server;
    private int port;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a server on port 0 so that Jetty picks an available port.
        server = CIServer.createServer(0);
        server.start();

        // Retrieve the actual port number that was assigned.
        port = server.getURI().getPort();
        assertTrue(port > 0, "Port should be greater than 0");
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    @Test
    public void testDoGet() throws Exception {
        // Since the GithubWebhook servlet is mapped to "/", we expect a 200 OK response.
        URI uri = new URI("http://localhost:" + port + "/");
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        // Verify that the response code is 200.
        assertEquals(200, responseCode, "Expected 200 response code");

        // Read the response body.
        String responseBody = new BufferedReader(new InputStreamReader(connection.getInputStream()))
            .lines().collect(Collectors.joining("\n"));
        assertTrue(responseBody.contains("This servlet is up and running."), 
                   "Response should contain the expected message.");
    }
}
