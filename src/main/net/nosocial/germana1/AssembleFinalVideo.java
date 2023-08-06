/**
 * Copyright (c) 2023 Ivan Khvostishkov & NoSocial.Net
 */
package net.nosocial.germana1;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.File;

import static net.nosocial.germana1.GenerateVideos.countNarratedFiles;

public class AssembleFinalVideo {
    private static final String FILE_NAME = "german-a1-phrases.mp4";
    public static final String PATH = "goethe_de/" + FILE_NAME;
    public static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion("eu-west-1")
            .build();

    public static void main(String[] args) {
        System.out.println("German A1 Trainer Tool (c) 2023 by NoSocial.Net");

        System.out.println("Generating randomized file list...");

        int count = countNarratedFiles();
        int totalPhrases = count / 9;
        System.out.println("Found " + count + " files for " + totalPhrases + " phrases");

        // generate array of numbers from 1 to totalPhrases and randomize it
        int[] randomized = new int[totalPhrases];
        for (int i = 0; i < totalPhrases; i++) {
            randomized[i] = i + 1;
        }
        System.out.println("Randomizing file list...");

        int seed = 42;
        System.out.println("Random seed: " + seed);
        java.util.Random random = new java.util.Random(42);

        for (int k = 0; k < 100; k++) {
            for (int i = 0; i < totalPhrases; i++) {
                int j = (int) (random.nextDouble() * totalPhrases);
                int temp = randomized[i];
                randomized[i] = randomized[j];
                randomized[j] = temp;
            }
        }

        // print array
        for (int i = 0; i < totalPhrases; i++) {
            System.out.print(randomized[i] + " ");
        }
        System.out.println();

        File fileList = new File("./out/mp4-file-list.txt");

        // write into file all the randomized numbers
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(fileList);
            for (int i = 0; i < totalPhrases; i++) {
                pw.println(String.format("file ./videos/" +
                        NarratePhrases.MP3_FILE_NAME_DE.replace(".mp3", ".mp4"), randomized[i]));
                pw.println(String.format("file ./videos/" +
                        NarratePhrases.MP3_FILE_NAME_EN.replace(".mp3", ".mp4"), randomized[i]));
                pw.println(String.format("file ./videos/" +
                        NarratePhrases.MP3_FILE_NAME_DE_SLOW.replace(".mp3", ".mp4"), randomized[i]));
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
