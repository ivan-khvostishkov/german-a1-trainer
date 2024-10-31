package net.nosocial.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtil {
    public static boolean checkOutput(ProcessBuilder builder) throws IOException, InterruptedException {
        // start the process
        Process p = builder.start();

        // Handle output and error streams in separate threads to prevent deadlock
        Thread outputThread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread errorThread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Start both threads
        outputThread.start();
        errorThread.start();

        // Wait for the process to complete
        int exitCode = p.waitFor();

        // Wait for the output threads to finish
        outputThread.join();
        errorThread.join();

        return exitCode == 0;
    }
}
