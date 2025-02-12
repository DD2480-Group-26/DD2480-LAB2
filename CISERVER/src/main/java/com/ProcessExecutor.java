package com;

import java.io.IOException;

/*
 * To make testing easier do we need to implement a interface for the process
 * 
 */
public interface ProcessExecutor {
    ProcessResult execute(ProcessBuilder pb) throws IOException, InterruptedException;
}


