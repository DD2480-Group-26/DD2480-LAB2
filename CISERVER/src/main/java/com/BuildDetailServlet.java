package com;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BuildDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String id = req.getParameter("id");
            BuildStatus status = FileBuildStatusStore.getStatusById(id);
            
            resp.setContentType("text/html");
            try (PrintWriter out = resp.getWriter()) {
                out.println("<html><head><title>Build Detail</title></head><body>");
                if (status != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy--dd--MM HH:mm:ss");
                    String date = sdf.format(new Date(status.getTimestamp()));
                    
                    out.println("<h1>Build Detail for ID: " + status.getId() + "</h1>");
                    out.println("<p><strong>Repository:</strong> " + status.getRepoName() + "</p>");
                    out.println("<p><strong>Commit SHA:</strong> " + status.getCommitSHA() + "</p>");
                    out.println("<p><strong>Branch:</strong> " + status.getBranch() + "</p>");
                    out.println("<p><strong>Status:</strong> " + (status.isSuccess() ? "Success" : "Failure") + "</p>");
                    out.println("<p><strong>Date:</strong> " + date + "</p>");
                    out.println("<p><strong>Details:</strong> " + status.getDetails() + "</p>");
                } else {
                    out.println("<p>Build status not found for id: " + id + "</p>");
                }
                out.println("<p><a href='/builds'>Back to build list</a></p>");
                out.println("</body></html>");
            }
        } catch (IOException e) {
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing build detail");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            // Catch any unexpected exceptions
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error occurred");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
