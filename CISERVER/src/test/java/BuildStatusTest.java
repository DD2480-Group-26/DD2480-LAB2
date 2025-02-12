package com;

import org.junit.jupiter.api.Test;

import com.BuildStatus;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class BuildStatusTest {

    // Test the constructor of the BuildStatus class to ensure that the ID and timestamp are set correctly.
    @Test
    void constructor_ShouldSetIdAndTimestamp() {
        long beforeCreation = System.currentTimeMillis();
        BuildStatus status = new BuildStatus("my-repo", "abc123", "main", true, "Success");
        long afterCreation = System.currentTimeMillis();

        assertNotNull(status.getId());
        assertDoesNotThrow(() -> UUID.fromString(status.getId()), "ID should be a valid UUID");
        
        long timestamp = status.getTimestamp();
        assertTrue(timestamp >= beforeCreation && timestamp <= afterCreation, 
            "Timestamp should be within the test execution time range");
    }

    // Test the constructor of the BuildStatus class to ensure that the fields are initialized correctly.
    @Test
    void constructor_ShouldInitializeFieldsCorrectly() {
        String repoName = "test-repo";
        String commitSHA = "1a2b3c";
        String branch = "dev";
        boolean success = false;
        String details = "Build failed due to error";

        BuildStatus status = new BuildStatus(repoName, commitSHA, branch, success, details);

        assertEquals(repoName, status.getRepoName());
        assertEquals(commitSHA, status.getCommitSHA());
        assertEquals(branch, status.getBranch());
        assertEquals(success, status.isSuccess());
        assertEquals(details, status.getDetails());
    }

    // Test the setters and getters of the BuildStatus class to ensure that the fields are updated correctly.
    @Test
    void settersAndGetters_ShouldUpdateFields() {
        BuildStatus status = new BuildStatus();
        String id = UUID.randomUUID().toString();
        String repoName = "new-repo";
        String commitSHA = "xyz789";
        String branch = "feature";
        boolean success = true;
        String details = "Updated details";
        long timestamp = 123456789L;

        status.setId(id);
        status.setRepoName(repoName);
        status.setCommitSHA(commitSHA);
        status.setBranch(branch);
        status.setSuccess(success);
        status.setDetails(details);
        status.setTimestamp(timestamp);

        assertEquals(id, status.getId());
        assertEquals(repoName, status.getRepoName());
        assertEquals(commitSHA, status.getCommitSHA());
        assertEquals(branch, status.getBranch());
        assertEquals(success, status.isSuccess());
        assertEquals(details, status.getDetails());
        assertEquals(timestamp, status.getTimestamp());
    }

    // Test the default constructor of the BuildStatus class to ensure that it can handle null and empty values.
    @Test
    void handleNullAndEmptyValues() {
        BuildStatus status = new BuildStatus(null, null, null, false, null);

        assertNull(status.getRepoName());
        assertNull(status.getCommitSHA());
        assertNull(status.getBranch());
        assertNull(status.getDetails());

        status.setRepoName("");
        status.setDetails("");

        assertEquals("", status.getRepoName());
        assertEquals("", status.getDetails());
    }

    // Test the success field of the BuildStatus class to ensure that it toggles correctly.
    @Test
    void successField_ShouldToggleCorrectly() {
        BuildStatus status = new BuildStatus();

        status.setSuccess(true);
        assertTrue(status.isSuccess());

        status.setSuccess(false);
        assertFalse(status.isSuccess());
    }
}