package com;

import java.io.File;

public class TestGithubWebhook extends GithubWebhook {
    private boolean simulateTestFailure = false;
    private boolean simulateCompileFailure = false;

    public void setSimulateTestFailure(boolean simulateTestFailure) {
        this.simulateTestFailure = simulateTestFailure;
    }

    public void setSimulateCompileFailure(boolean simulateCompileFailure) {
        this.simulateCompileFailure = simulateCompileFailure;
    }

    // Override runBuildAtCommit to bypass the real token check and external commands.
    @Override
    protected void runBuildAtCommit(String owner, String repo, String commitSHA) throws Exception {
        System.out.println("Simulated runBuildAtCommit for commit " + commitSHA);
        // Instead of performing real clone/checkout/build, simulate the phases:
        runCompilePhase(null);
        runTestPhase(null);
    }

    @Override
    protected void runTestPhase(File workspace) throws Exception {
        if (simulateTestFailure) {
            System.out.println("Simulated test phase failure.");
            throw new Exception("Simulated test phase failure");
        } else {
            System.out.println("Simulated test phase success.");
            // Simulate success by doing nothing.
        }
    }

    @Override
    protected void runCompilePhase(File workspace) throws Exception {
        if (simulateCompileFailure) {
            System.out.println("Simulated compile phase failure.");
            throw new Exception("Simulated compile phase failure");
        } else {
            System.out.println("Simulated compile phase success.");
            // Simulate success by doing nothing.
        }
    }
}
