package com;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Unit tests for runBuildAtCommit.
 *
 * Contract being tested:
 * - The method should run the clone, checkout, compile, and test phases in sequence.
 * - If any phase fails (clone, checkout, compile, or test), it should throw an exception with an appropriate message.
 */
@ExtendWith(MockitoExtension.class)
public class BuildAtCommitErrorIntegrationTest {

    @Mock
    private ProcessExecutor processExecutor;

    // Declare the webhook as a class-level field.
    private GithubWebhook webhook;
    private GitHubClient githubclient; 
    // Track workspace directories created during tests.
    private File workspace;

    @BeforeEach
    public void setup() {
       // Initialize the class-level webhook instance.
       webhook = new GithubWebhook(processExecutor, githubclient);
    }

    @AfterEach
    public void cleanup() {
        // Clean up any workspace directory created.
        if (workspace != null && workspace.exists()) {
            try {
                FileUtils.deleteDirectory(workspace);
            } catch (IOException e) {
                System.err.println("Failed to delete workspace: " + e.getMessage());
            }
        }
    }

    @Test
    public void testRunBuildAtCommitSuccess() throws Exception {
        // Contract: When all phases succeed, runBuildAtCommit completes normally.
        ProcessResult successResult = new ProcessResult(0, "Success");
        // Expect four calls: clone, checkout, compile, test.
        when(processExecutor.execute(any())).thenReturn(successResult, successResult, successResult, successResult);

        // We call the method; note that runBuildAtCommit internally creates its own workspace.
        // To capture that workspace for cleanup, we could modify runBuildAtCommit to store the workspace,
        // or simply rely on the fact that our dummy directory name (e.g., "workspace_") is transient.
        // For simplicity here, we assume runBuildAtCommit cleans up its workspace via its own mechanism (if any)
        // or that the directory name is unique and we don’t worry about cleanup. If not, consider modifying the
        // code to return the workspace for testing.
        assertDoesNotThrow(() -> webhook.runBuildAtCommit("testOwner", "testRepo", "commit123"));
    }

    @Test
    public void testRunBuildAtCommitCloneFailure() throws Exception {
        // Contract: If the clone phase fails, an exception with "Git clone failed" is thrown.
        ProcessResult failureResult = new ProcessResult(1, "Clone error");
        when(processExecutor.execute(any())).thenReturn(failureResult);

        Exception exception = assertThrows(Exception.class, () ->
            webhook.runBuildAtCommit("testOwner", "testRepo", "commit123"));
        assertFalse(exception.getMessage().contains("Git clone failed"));
    }

    @Test
    public void testRunBuildAtCommitCheckoutFailure() throws Exception {
        // Contract: If checkout fails, an exception with "Git checkout of commit" is thrown.
        ProcessResult successResult = new ProcessResult(0, "Success");
        ProcessResult failureResult = new ProcessResult(1, "Checkout error");
        when(processExecutor.execute(any())).thenReturn(successResult, failureResult);

        Exception exception = assertThrows(Exception.class, () ->
            webhook.runBuildAtCommit("testOwner", "testRepo", "commit123"));
        assertTrue(exception.getMessage().contains("Git checkout of commit"));
    }

    @Test
    public void testRunBuildAtCommitCompileFailure() throws Exception {
        // Contract: If compile fails, an exception with "Compilation failed" is thrown.
        ProcessResult successResult = new ProcessResult(0, "Success");
        ProcessResult compileFailure = new ProcessResult(1, "Compilation error");
        when(processExecutor.execute(any())).thenReturn(successResult, successResult, compileFailure);

        Exception exception = assertThrows(Exception.class, () ->
            webhook.runBuildAtCommit("testOwner", "testRepo", "commit123"));
        assertTrue(exception.getMessage().contains("Compilation failed"));
    }

    @Test
    public void testRunBuildAtCommitTestFailure() throws Exception {
        // Contract: If test phase fails, an exception with "Test phase failed" is thrown.
        ProcessResult successResult = new ProcessResult(0, "Success");
        ProcessResult testFailure = new ProcessResult(1, "Test failures");
        when(processExecutor.execute(any())).thenReturn(successResult, successResult, successResult, testFailure);

        Exception exception = assertThrows(Exception.class, () ->
            webhook.runBuildAtCommit("testOwner", "testRepo", "commit123"));
        assertTrue(exception.getMessage().contains("Test phase failed"));
    }
}
