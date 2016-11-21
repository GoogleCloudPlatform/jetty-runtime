package com.google.cloud.runtime.jetty.testing;

import org.eclipse.jetty.toolchain.test.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Utilities for executing and collecting output from command line processes.
 */
public final class ProcessUtil {
  private static final ExecutorService executor = Executors.newFixedThreadPool(5);

  /**
   * Execute a command line.
   * <p>
   * Report output from command line to Writer
   * </p>
   *
   * @param output where to put the output from the execution of the command line
   * @param args   the command line arguments
   * @return the exit code from the execution
   */
  public static int exec(OutputStream output, String... args)
      throws IOException, InterruptedException, ExecutionException {
    System.out.printf("exec(%s)%n", Arrays.toString(args));
    Process process = Runtime.getRuntime().exec(args);

    InputStream in = process.getInputStream();

    Future<Void> fut = executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        IO.copy(in, output);
        return null;
      }
    });

    fut.get();

    return process.waitFor();
  }
}
