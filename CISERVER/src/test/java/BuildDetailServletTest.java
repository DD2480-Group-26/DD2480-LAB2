package com;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BuildDetailServletTest {

    private BuildDetailServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    public void setUp() throws Exception {
        servlet = new BuildDetailServlet();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        Mockito.when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Clear the store for a clean state.
        FileBuildStatusStore.getStatuses().clear();
    }

    @Test
    /**
     * Test that when no build status is found (with a nonexistent id), the output contains
     * an appropriate message.
     */
    public void testDoGet_statusNotFound() throws Exception {
        Mockito.when(request.getParameter("id")).thenReturn("nonexistent");
        servlet.doGet(request, response);
        String output = responseWriter.toString();
        assertTrue(output.contains("Build status not found for id: nonexistent"),
                "Output should indicate that no build status was found for the given id.");
    }

    @Test
    /**
     * Test that when a build status is found, its details are rendered correctly including
     * formatted date, repository name, commit SHA, branch, and details.
     */
    public void testDoGet_statusFound() throws Exception {
        BuildStatus status = new BuildStatus();
        status.setId("456");
        status.setRepoName("AnotherRepo");
        status.setCommitSHA("fghij67890");
        status.setBranch("develop");
        status.setDetails("Build failed due to errors.");
        status.setSuccess(false);
        long now = System.currentTimeMillis();
        status.setTimestamp(now);

        FileBuildStatusStore.addStatus(status);
        Mockito.when(request.getParameter("id")).thenReturn("456");

        servlet.doGet(request, response);
        String output = responseWriter.toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy--dd--MM HH:mm:ss");
        String expectedDate = sdf.format(new Date(now));

        assertTrue(output.contains("AnotherRepo"), "Output should contain the repository name.");
        assertTrue(output.contains("fghij67890"), "Output should contain the commit SHA.");
        assertTrue(output.contains("Build failed due to errors."), "Output should contain the build details.");
        assertTrue(output.contains(expectedDate), "Output should contain the correctly formatted date.");
    }

    @Test
    /**
     * Test that if obtaining the writer from the response throws an IOException,
     * the servlet calls sendError with the proper HTTP status code and message.
     */
    public void testDoGet_whenWriterThrowsException() throws Exception {
        // Force response.getWriter() to throw an IOException.
        Mockito.when(response.getWriter()).thenThrow(new IOException("Test IOException"));
        Mockito.when(request.getParameter("id")).thenReturn("anyId");

        servlet.doGet(request, response);
        // Verify that sendError is called with HTTP 500.
        Mockito.verify(response)
               .sendError(Mockito.eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
                          Mockito.contains("Error processing build detail"));
    }

    @Test
    /**
     * Test that when the "id" parameter is missing (i.e. null),
     * the servlet handles it gracefully by indicating that no build status was found.
     */
    public void testDoGet_nullIdParameter() throws Exception {
        Mockito.when(request.getParameter("id")).thenReturn(null);
        servlet.doGet(request, response);
        String output = responseWriter.toString();
        assertTrue(output.contains("Build status not found for id: null"),
                   "Output should indicate that no build status was found when the id parameter is null.");
    }

    @Test
    /**
     * Test that when a build status is found but some fields are null,
     * the servlet still renders the page (with "null" displayed where appropriate).
     */
    public void testDoGet_statusFound_withNullFields() throws Exception {
        BuildStatus status = new BuildStatus();
        status.setId("789");
        // Intentionally setting some fields to null.
        status.setRepoName(null);
        status.setCommitSHA(null);
        status.setBranch(null);
        status.setDetails(null);
        status.setSuccess(true);
        long now = System.currentTimeMillis();
        status.setTimestamp(now);

        FileBuildStatusStore.addStatus(status);
        Mockito.when(request.getParameter("id")).thenReturn("789");

        servlet.doGet(request, response);
        String output = responseWriter.toString();

        assertTrue(output.contains("Build Detail for ID: 789"),
                   "Output should contain the build detail header with the correct id.");
        // Depending on your implementation, "null" may be printed for missing fields.
        assertTrue(output.contains("null"), "Output should display 'null' for fields that are not set.");
    }
}
