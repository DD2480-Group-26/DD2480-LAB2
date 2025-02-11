package com;  // or your preferred test package

import com.CIServer;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

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
    public void testServerResponds()  throws Exception {
        // Since no servlet is mapped for "/", the server will likely return a 404 (Not Found)
        URI uri = new URI("http://localhost:" + port + "/");
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        // Verify that the response code is 404.
        assertEquals(404, responseCode, "Expected 404 response code");
    }
}
