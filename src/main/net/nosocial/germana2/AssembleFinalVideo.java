/**
 * Copyright (c) 2023 Ivan Khvostishkov & NoSocial.Net
 */
package net.nosocial.germana2;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import net.nosocial.util.ProcessUtil;

import java.io.File;
import java.io.IOException;

import static net.nosocial.germana2.GenerateVideos.countNarratedFiles;

public class AssembleFinalVideo {
    private static final String FILE_NAME = "german-a2-phrases.mp4";
    public static final String PATH = "goethe_de/" + FILE_NAME;

    public static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider())
            .withRegion("eu-west-1")
            .build();

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("German A2 Hands-free Trainer (c) 2023-2024 by NoSocial.Net");

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

        assembleFinalVideo();

        System.out.println("Uploading " + FILE_NAME + " to S3...");
        s3Client.putObject(DownloadWordList.BUCKET_NAME, PATH, new File("out/" + FILE_NAME));

        System.out.println("Done!");
    }

    private static void assembleFinalVideo() throws IOException, InterruptedException {
        System.out.println("Generating " + FILE_NAME + "...");

        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-y", "-f", "concat", "-safe", "0",
                "-i", "./out/mp4-file-list.txt",
                "-c", "copy",
                "./out/" + FILE_NAME
        );
        if (!ProcessUtil.checkOutput(builder)) {
            System.out.println("Error assembling video");
            System.exit(1);
        }
    }

}
