/**
 * Copyright (c) 2023 Ivan Khvostishkov & NoSocial.Net
 */
package net.nosocial.germana2;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class DownloadWordList {

    public static final String BUCKET_NAME = "german-a1-trainer";
    public static final String PATH = "goethe_de/Goethe-Zertifikat_A2_Wortliste.pdf";
    public static final String FILE_URL = "https://www.goethe.de/pro/relaunch/prf/de/Goethe-Zertifikat_A2_Wortliste.pdf";

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("German A2 Hands-free Trainer (c) 2023-2024 by NoSocial.Net");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion("eu-west-1")
                .build();
        List<Bucket> buckets = s3Client.listBuckets();

        boolean bucketExists = false;
        for (Bucket b : buckets) {
            if (b.getName().equals(BUCKET_NAME)) {
                bucketExists = true;
                break;
            }
        }

        if (!bucketExists) {
            System.out.println("Bucket " + BUCKET_NAME + " does not exist. Creating...");
            s3Client.createBucket(BUCKET_NAME);
        } else {
            System.out.println("Bucket " + BUCKET_NAME + " exists.");
        }

        System.out.println("Downloading file to S3...");

        InputStream inputStream = new URI(FILE_URL).toURL().openStream();
        s3Client.putObject(BUCKET_NAME, PATH, inputStream, null);

        System.out.println("Done.");
    }
}
