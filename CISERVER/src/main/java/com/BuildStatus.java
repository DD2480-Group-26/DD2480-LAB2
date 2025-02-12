package com;

import java.util.UUID;

public class BuildStatus {
    private String id;       // Unique ID for the build
    private String repoName;
    private String commitSHA;
    private String branch;
    private boolean success;
    private String details;  // A short description or error summary
    private long timestamp;  // Build time in milliseconds since epoch
    private String logs;     // Optional: build logs

    // Default constructor (needed for Jackson)
    public BuildStatus() {}

    // Constructor that automatically assigns a unique ID and timestamp.
    public BuildStatus(String repoName, String commitSHA, String branch, boolean success, String details) {
        this.id = UUID.randomUUID().toString();
        this.repoName = repoName;
        this.commitSHA = commitSHA;
        this.branch = branch;
        this.success = success;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
        // logs == deails
    }

    // Getters and setters
    public String getId() {
         return id; 
    }

    public void setId(String id) { this.id = id; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public String getCommitSHA() { return commitSHA; }
    public void setCommitSHA(String commitSHA) { this.commitSHA = commitSHA; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

   // public String getLogs() { return logs; }
   // public void setLogs(String logs) { this.logs = logs; }
}