package com;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.BuildStatus;
import com.FileBuildStatusStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FileBuildStatusStoreTest {
    private static final String FILE_PATH = "build_statuses.json";
    private final ObjectMapper mapper = new ObjectMapper();

    // Delete the file and reset the static state before each test.
    @BeforeEach
    void setUp() throws Exception {
        Files.deleteIfExists(Paths.get(FILE_PATH));
        resetStaticState();
    }

    // Delete the file after each test. 
    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(FILE_PATH));
    }

    // Helper function: Reset the static state of the FileBuildStatusStore class.
    private void resetStaticState() throws Exception {
        Field statusesField = FileBuildStatusStore.class.getDeclaredField("statuses");
        statusesField.setAccessible(true);
        statusesField.set(null, new ArrayList<>());

        Field initialCountField = FileBuildStatusStore.class.getDeclaredField("initialCount");
        initialCountField.setAccessible(true);
        initialCountField.setInt(null, 0);
    }

    // Test the addStatus method of the FileBuildStatusStore class to ensure that the status is persisted to the file and the list is updated.
    @Test
    void addStatus_ShouldPersistToFileAndUpdateList() throws Exception {
        BuildStatus status = new BuildStatus("my-repo", "abc123", "main", true, "Success");
        
        FileBuildStatusStore.addStatus(status);
        
        List<BuildStatus> statuses = FileBuildStatusStore.getStatuses();
        assertEquals(1, statuses.size());
        assertEquals(status.getId(), statuses.get(0).getId());
        
        File file = new File(FILE_PATH);
        List<BuildStatus> fileStatuses = mapper.readValue(file, new TypeReference<List<BuildStatus>>() {});
        assertEquals(1, fileStatuses.size());
        assertEquals(status.getId(), fileStatuses.get(0).getId());
    }

    // Test the getStatusById method of the FileBuildStatusStore class to ensure that the correct status is returned.
    @Test
    void getStatusById_ShouldReturnCorrectStatus() {
        BuildStatus status1 = new BuildStatus("repo1", "sha1", "branch1", true, "Details1");
        BuildStatus status2 = new BuildStatus("repo2", "sha2", "branch2", false, "Details2");
        FileBuildStatusStore.addStatus(status1);
        FileBuildStatusStore.addStatus(status2);
        
        BuildStatus found = FileBuildStatusStore.getStatusById(status1.getId());
        assertNotNull(found);
        assertEquals(status1.getRepoName(), found.getRepoName());
        
        BuildStatus notFound = FileBuildStatusStore.getStatusById("invalid-id");
        assertNull(notFound);
    }

    // Test the getStatuses method of the FileBuildStatusStore class to ensure that all entries are returned.
    @Test
    void getStatuses_ShouldReturnAllEntries() {
        assertEquals(0, FileBuildStatusStore.getStatuses().size());
        
        FileBuildStatusStore.addStatus(new BuildStatus("repo", "sha", "branch", true, "Details"));
        FileBuildStatusStore.addStatus(new BuildStatus("repo", "sha", "branch", true, "Details"));
        assertEquals(2, FileBuildStatusStore.getStatuses().size());
    }

    // Test that addStatus method of the FileBuildStatusStore class is thread-safe.
    @Test
    void concurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        Thread t1 = new Thread(() -> FileBuildStatusStore.addStatus(new BuildStatus("repo1", "sha1", "branch1", true, "Details")));
        Thread t2 = new Thread(() -> FileBuildStatusStore.addStatus(new BuildStatus("repo2", "sha2", "branch2", false, "Details")));
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        assertEquals(2, FileBuildStatusStore.getStatuses().size());
    }
}