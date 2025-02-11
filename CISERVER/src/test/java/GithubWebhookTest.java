package com;  // or your preferred test package

import com.CIServer;  // if needed
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ReadListener;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.io.*;
public class GithubWebhookTest {

    @Test
    public void testDoPostWithValidPayload() throws Exception {
        // Prepare a sample JSON payload.
        String payload = "{"
                + "\"after\": \"commit123\","
                + "\"repository\": {"
                + "    \"name\": \"TestRepo\","
                + "    \"owner\": { \"login\": \"TestOwner\" }"
                + "},"
                + "\"ref\": \"refs/heads/main\""
                + "}";
        
        // Create a mock HttpServletRequest.
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            private final ByteArrayInputStream bais = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
            @Override
            public int read() throws IOException {
                return bais.read();
            }
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }
            @Override
            public boolean isReady() {
                return true;
            }
            @Override
            public void setReadListener(ReadListener readListener) {
                
            }
        });
        
        // Create a mock HttpServletResponse and capture its output.
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);
        
        // Instantiate your servlet and invoke doPost.
        GithubWebhook servlet = new GithubWebhook();
        servlet.doPost(request, response);
        
        pw.flush();
        String output = sw.toString();
        
        // Assert that the output contains "Build succeeded." per your current logic.
        assertTrue(output.contains("Build succeeded."));
    }


}
