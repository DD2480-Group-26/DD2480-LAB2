package com;
import java.io.IOException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.DefaultGitHubClient;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

public class DefaultGitHubClientTest {
    private MockWebServer mockWebServer;
    private DefaultGitHubClient client;

    @BeforeEach
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new DefaultGitHubClient(mockWebServer.url("/").toString());
    }

    @AfterEach
    public void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void postStatus_success() throws Exception {
        new EnvironmentVariables("GITHUB_TOKEN", "dummy_token").execute(() -> {
            // Fake a successful response
            mockWebServer.enqueue(new MockResponse().setResponseCode(201));
            
            client.postStatus("owner", "repo", "commitSHA", "success");
            
            // Verify the request
            RecordedRequest request = mockWebServer.takeRequest();

            assertEquals("POST", request.getMethod());
            assertEquals("application/json", request.getHeader("Content-Type"));
            assertEquals("token dummy_token", request.getHeader("Authorization"));
            assertTrue(request.getBody().readUtf8().contains("\"state\": \"success\""));
        });
    }

    @Test
    public void getDescription_validStates_returnsCorrectDescription() {
        assertEquals("Build succeeded", client.getDescription("success"));
        assertEquals("Build failed", client.getDescription("failure"));
        assertEquals("Build encountered an error", client.getDescription("error"));
        assertEquals("Build is pending", client.getDescription("pending"));
        assertEquals("Build status: unknown", client.getDescription("unknown"));
    }

    @Test 
    public void postStatus_missingToken_throwsError() throws Exception {
        new EnvironmentVariables("GITHUB_TOKEN", null).execute(() -> {
            assertThrows(RuntimeException.class, () -> {
                client.postStatus("owner", "repo", "commitSHA", "success");
            });
        });
    }
}
