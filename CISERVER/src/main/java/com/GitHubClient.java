package com;


/*
 * Interface for posting status to GitHub
 * 
 */
public interface GitHubClient {
    void postStatus(String owner, String repo, String commitSHA, String state);
}