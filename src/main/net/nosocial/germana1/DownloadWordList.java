package net.nosocial.germana1;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class DownloadWordList {

    public static final String BUCKET_NAME = "german-a1-trainer";
    public static final String PATH = "goethe_de/A1_SD1_Wortliste_02.pdf";

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("German A1 Trainer Tool (c) 2023 by NoSocial.Net");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
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

        String fileUrl = "https://www.goethe.de/pro/relaunch/prf/de/A1_SD1_Wortliste_02.pdf";

        InputStream inputStream = new URI(fileUrl).toURL().openStream();
        s3Client.putObject(BUCKET_NAME, PATH, inputStream, null);

        System.out.println("Done.");
    }
}
