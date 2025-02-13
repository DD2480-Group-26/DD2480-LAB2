package com;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.BuildNotificationServlet;
import com.BuildStatus;
import com.FileBuildStatusStore;

public class BuildNotificationServletTest {

    private BuildNotificationServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

  
    // Variable to hold the original file content
    private static String originalFileContent;
    // The path to the persistent file.
    private static final String FILE_PATH = "build_statuses.json";

    @BeforeAll
    public static void backupFile() throws IOException {
        Path path = Paths.get(FILE_PATH);
        if (Files.exists(path)) {
            originalFileContent = Files.readString(path, StandardCharsets.UTF_8);
        } else {
            originalFileContent = null;
        }
    }

    @AfterAll
    public static void restoreFile() throws IOException {
        Path path = Paths.get(FILE_PATH);
        if (originalFileContent != null) {
            // Restore the original content.
            Files.writeString(path, originalFileContent, StandardCharsets.UTF_8);
        } else {
            // If the file did not originally exist, remove any file created during tests.
            Files.deleteIfExists(path);
        }
    }


    @BeforeEach
    public void setUp() throws Exception {
        servlet = new BuildNotificationServlet();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        Mockito.when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Clear the store for a clean state.
        FileBuildStatusStore.getStatuses().clear();
        FileBuildStatusStore.initialCount = 0;
    }

    @Test
    /**
     * Test that when there are no build notifications for the session,
     * the servlet outputs a message indicating that no notifications exist.
     */
    public void testDoGet_noNotifications() throws Exception {
        servlet.doGet(request, response);
        String output = responseWriter.toString();
        assertTrue(output.contains("No build notifications yet for this session"),
                "Output should indicate no notifications.");
    }

    @Test
    /**
     * Test that when a build status is added to the store, the notification servlet
     * correctly displays the build details.
     */
    public void testDoGet_withNotification() throws Exception {
        // Create and add a build status.
        BuildStatus status = new BuildStatus();
        status.setId("123");
        status.setRepoName("TestRepo");
        status.setCommitSHA("abcde12345");
        status.setBranch("main");
        status.setDetails("Build succeeded.");
        status.setSuccess(true);
        status.setTimestamp(System.currentTimeMillis());

        FileBuildStatusStore.addStatus(status);
        servlet.doGet(request, response);
        String output = responseWriter.toString();

        assertTrue(output.contains("TestRepo"), "Output should contain the repository name.");
        assertTrue(output.contains("abcde12345"), "Output should contain the commit SHA.");
        assertTrue(output.contains("Build succeeded."), "Output should contain the build details.");
    }

    @Test
    /**
     * Test that if obtaining the writer throws an IOException,
     * the servlet calls sendError with the proper HTTP status code and message.
     */
    public void testDoGet_whenWriterThrowsException() throws Exception {
        // Force response.getWriter() to throw an IOException.
        Mockito.when(response.getWriter()).thenThrow(new IOException("Test IOException"));
        servlet.doGet(request, response);
        // Verify that sendError is called with HTTP 500.
        Mockito.verify(response)
               .sendError(Mockito.eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
                          Mockito.contains("Error processing build notification"));
    }

    @Test
    /**
     * Test that when the static statuses list is corrupted (set to null),
     * the servlet handles the situation gracefully by indicating that no notifications are available.
     */
    public void testDoGet_withNullStatuses() throws Exception {
        // Simulate a corrupted static state by setting statuses to null.
        Field statusesField = FileBuildStatusStore.class.getDeclaredField("statuses");
        statusesField.setAccessible(true);
        statusesField.set(null, null);

        servlet.doGet(request, response);
        String output = responseWriter.toString();
        // Expect that even with null statuses the servlet shows a message (or at least does not crash).
        assertTrue(output.contains("No build notifications yet for this session"),
                "Output should indicate no notifications even when statuses is null");

        // Restore statuses to a valid state for subsequent tests.
        statusesField.set(null, new ArrayList<BuildStatus>());
    }

    

    @Test
    /**
     * Test that when there are no new build notifications (i.e., the initial count
     * equals the current number of statuses), the servlet outputs an appropriate message.
     */
    public void testDoGet_noNewNotifications() throws Exception {
        // Add a status to simulate a preexisting build status.
        BuildStatus status = new BuildStatus();
        status.setId("010");
        status.setRepoName("PreexistingRepo");
        status.setCommitSHA("presha");
        status.setBranch("main");
        status.setDetails("Preexisting build status.");
        status.setSuccess(true);
        status.setTimestamp(System.currentTimeMillis());
        FileBuildStatusStore.addStatus(status);

        // Set initialCount to simulate that the session has already seen this notification.
        FileBuildStatusStore.initialCount = FileBuildStatusStore.getStatuses().size();

        servlet.doGet(request, response);
        String output = responseWriter.toString();

        assertTrue(output.contains("No build notifications yet for this session"),
                   "Output should indicate that there are no new notifications.");
    }
}
