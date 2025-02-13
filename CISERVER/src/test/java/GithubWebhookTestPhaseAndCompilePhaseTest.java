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
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Unit tests for the individual Maven phase methods.
 *
 * Contract being tested:
 * - runCompilePhase: When the process returns exit code 0, the method completes without exception;
 *   when it returns nonzero, an exception is thrown with a message containing "Compilation failed".
 * - runTestPhase: Similarly, success (exit code 0) completes normally; a nonzero exit code causes an exception.
 */
@ExtendWith(MockitoExtension.class)
public class GithubWebhookTestPhaseAndCompilePhaseTest {

    @Mock
    private ProcessExecutor processExecutor;

    // Declare the webhook as a class-level field.
    private GithubWebhook webhook;
    private GitHubClient githubclient; 
    // Store a reference to the workspace directory created in each test.
    private File workspace;

    @BeforeEach
    public void setup() {
        // Inject mocks into the webhook.
        webhook = new GithubWebhook(processExecutor,githubclient );
    }

    @AfterEach
    public void cleanup() {
        // Clean up the workspace directory if it was created.
        if (workspace != null && workspace.exists()) {
            try {
                FileUtils.deleteDirectory(workspace);
            } catch (IOException e) {
                System.err.println("Failed to delete workspace: " + e.getMessage());
            }
        }
    }

    @Test
    public void testRunCompilePhaseSuccess() throws Exception {
        // Contract: A successful compile (exit code 0) does not throw an exception.
        ProcessResult successResult = new ProcessResult(0, "Compilation successful.");
        when(processExecutor.execute(any(ProcessBuilder.class))).thenReturn(successResult);

        // Create a dummy workspace.
        workspace = new File("dummy_workspace_compile");
        workspace.mkdirs();

        // Act & Assert
        assertDoesNotThrow(() -> webhook.runCompilePhase(workspace));

        // Verify the expected Maven compile command is used.
        verify(processExecutor).execute(argThat(new ProcessBuilderMatcher(new String[] {
            "mvn", "-B", "-f", "CISERVER/pom.xml", "clean", "compile"
        })));
    }

    @Test
    public void testRunCompilePhaseFailure() throws Exception {
        // Contract: When the process returns a non-zero exit code, an exception is thrown.
        ProcessResult failureResult = new ProcessResult(1, "Compilation error.");
        when(processExecutor.execute(any(ProcessBuilder.class))).thenReturn(failureResult);

        workspace = new File("dummy_workspace_compile_fail");
        workspace.mkdirs();

        Exception exception = assertThrows(Exception.class, () -> webhook.runCompilePhase(workspace));
        assertTrue(exception.getMessage().contains("Compilation failed"),
                "Exception message should contain 'Compilation failed'");
    }

    @Test
    public void testRunTestPhaseSuccess() throws Exception {
        // Contract: A successful test phase (exit code 0) completes normally.
        ProcessResult successResult = new ProcessResult(0, "Tests passed successfully.");
        when(processExecutor.execute(any(ProcessBuilder.class))).thenReturn(successResult);

        workspace = new File("dummy_workspace_test");
        workspace.mkdirs();

        assertDoesNotThrow(() -> webhook.runTestPhase(workspace));

        // Verify that the expected Maven test command is used.
        verify(processExecutor).execute(argThat(new ProcessBuilderMatcher(new String[] {
            "mvn", "-B", "-f", "CISERVER/pom.xml", "test"
        })));
    }

    @Test
    public void testRunTestPhaseFailure() throws Exception {
        // Contract: A failing test phase (non-zero exit code) should throw an exception.
        ProcessResult failureResult = new ProcessResult(1, "Test failures occurred.");
        when(processExecutor.execute(any(ProcessBuilder.class))).thenReturn(failureResult);

        workspace = new File("dummy_workspace_test_fail");
        workspace.mkdirs();

        Exception exception = assertThrows(Exception.class, () -> webhook.runTestPhase(workspace));
        assertTrue(exception.getMessage().contains("Test phase failed"),
                "Exception message should contain 'Test phase failed'");
    }

    // Helper matcher to verify that ProcessBuilder is created with the expected command.
    static class ProcessBuilderMatcher implements ArgumentMatcher<ProcessBuilder> {
        private final String[] expectedCommand;

        public ProcessBuilderMatcher(String[] expectedCommand) {
            this.expectedCommand = expectedCommand;
        }

        @Override
        public boolean matches(ProcessBuilder pb) {
            return pb.command().equals(java.util.Arrays.asList(expectedCommand));
        }
    }
}
