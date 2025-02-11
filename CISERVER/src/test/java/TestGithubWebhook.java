package com;

public class TestGithubWebhook extends GithubWebhook {
    private boolean simulateTestFailure = false;
    private boolean simulateCompileFailure = false;

    public void setSimulateTestFailure(boolean simulateTestFailure) {
        this.simulateTestFailure = simulateTestFailure;
    }

    public void setSimulateCompileFailure(boolean simulateCompileFailure) {
        this.simulateCompileFailure = simulateCompileFailure;
    }

    @Override
    protected void runTestPhase() throws Exception {
        if (simulateTestFailure) {
            System.out.println("Simulated test phase failure.");
            throw new Exception("Simulated test phase failure");
        } else {
            System.out.println("Simulated test phase success.");
            // Simulate success by doing nothing.
        }
    }

    @Override
    protected void runCompilePhase() throws Exception {
        if (simulateCompileFailure) {
            System.out.println("Simulated compile phase failure.");
            throw new Exception("Simulated compile phase failure");
        } else {
            System.out.println("Simulated compile phase success.");
            // Simulate success by doing nothing.
        }
    }
}
