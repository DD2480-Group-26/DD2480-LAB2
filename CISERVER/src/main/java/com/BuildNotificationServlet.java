package com;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BuildNotificationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            // Get the list of build statuses
            List<BuildStatus> statuses = FileBuildStatusStore.getStatuses();
            // Optional: Ensure statuses is not null even if corrupted
            if (statuses == null) {
                statuses = new ArrayList<>();
            }
            int initialCount = FileBuildStatusStore.initialCount;
            BuildStatus latest = (statuses.size() > initialCount)
                    ? statuses.get(statuses.size() - 1)
                    : null;
            
            resp.setContentType("text/html");
            
            // Try-with-resources to ensure writer is closed properly.
            try (PrintWriter out = resp.getWriter()) {
                out.println("<html><head><title>Build Notification</title>");
                out.println("<meta http-equiv='refresh' content='2'>");
                out.println("<style>");
                out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
                out.println(".notification { border: 1px solid #ccc; padding: 10px; margin: 10px 0; }");
                out.println(".success { background-color: #dff0d8; color: #3c763d; }");
                out.println(".failure { background-color: #f2dede; color: #a94442; }");
                out.println("</style>");
                out.println("</head><body>");
                out.println("<h1>Latest Build Notification</h1>");
                if (latest != null) {
                    String statusClass = latest.isSuccess() ? "success" : "failure";
                    out.println("<div class='notification " + statusClass + "'>");
                    out.println("<p><strong>Repository:</strong> " + latest.getRepoName() + "</p>");
                    out.println("<p><strong>Commit SHA:</strong> " + latest.getCommitSHA() + "</p>");
                    out.println("<p><strong>Branch:</strong> " + latest.getBranch() + "</p>");
                    out.println("<p><strong>Status:</strong> " + (latest.isSuccess() ? "Success" : "Failure") + "</p>");
                    out.println("<p><strong>Details:</strong> " + latest.getDetails() + "</p>");
                    out.println("</div>");
                } else {
                    out.println("<p>No build notifications yet for this session.</p>");
                }
                out.println("</body></html>");
            }
        } catch (IOException e) {
            // Handle exceptions from response.getWriter() and writing the response
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing build notification");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error occurred");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
