package com.bytelegend.game;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class Shell {
    private final File workingDir;

    Shell(File workingDir) {
        this.workingDir = workingDir;
    }

    ExecResult execSuccessfully(String... args) throws Exception {
        return exec(args).assertZeroExit();
    }

    ExecResult exec(String... args) throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(workingDir);

        Process process = pb.start();

        CountDownLatch latch = new CountDownLatch(2);
        copyStream(process.getInputStream(), stdout, latch);
        copyStream(process.getErrorStream(), stderr, latch);

        boolean result = process.waitFor(1, TimeUnit.MINUTES);
        boolean latchResult = latch.await(1, TimeUnit.MINUTES);
        if (!result || !latchResult) {
            throw new IllegalStateException("Timeout waiting " + Arrays.toString(args) +
                    "\nstdout:\n" + stdout + "\nstderr:\n" + stderr);
        }
        return new ExecResult(
                Arrays.asList(args),
                process.exitValue(),
                workingDir.getAbsolutePath(),
                stdout.toString(),
                stderr.toString()
        );
    }

    private static void copyStream(InputStream is, OutputStream os, CountDownLatch latch) {
        new Thread(() -> {
            try {
                IOUtils.copy(is, os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        }).start();
    }
}

class ExecResult {
    final List<String> args;
    final int exitValue;
    final String stdout;
    final String stderr;
    final String workingDir;

    ExecResult(List<String> args, int exitValue, String workingDir, String stdout, String stderr) {
        this.args = args;
        this.workingDir = workingDir;
        this.exitValue = exitValue;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    String getOutput() {
        return stdout + "\n" + stderr;
    }

    ExecResult assertZeroExit() {
        withLog();
        assert exitValue == 0;
        return this;
    }

    ExecResult withLog() {
        System.out.printf("Exec %s in %s exited with %s:\n%s", args, workingDir, exitValue, getOutput());
        return this;
    }
}
