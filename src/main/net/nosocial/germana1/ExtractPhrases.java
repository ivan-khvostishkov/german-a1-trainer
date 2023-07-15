package net.nosocial.germana1;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
//import com.amazonaws.services.textract.AmazonTextractClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ExtractPhrases {
    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("German A1 Trainer Tool (c) 2023 by NoSocial.Net");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("eu-west-1")
                .build();
        List<Bucket> buckets = s3Client.listBuckets();

        boolean bucketExists = false;
        String bucketName = "german-a1-trainer";
        for (Bucket b : buckets) {
            if (b.getName().equals(bucketName)) {
                bucketExists = true;
                break;
            }
        }

        if (!bucketExists) {
            System.out.println("Bucket " + bucketName + " does not exist. Creating...");
            s3Client.createBucket(bucketName);
        } else {
            System.out.println("Bucket " + bucketName + " exists.");
        }

        System.out.println("Downloading file...");

        String fileUrl = "https://www.goethe.de/pro/relaunch/prf/de/A1_SD1_Wortliste_02.pdf";

        InputStream inputStream = new URI(fileUrl).toURL().openStream();
        s3Client.putObject(bucketName, "goethe_de/A1_SD1_Wortliste_02.pdf", inputStream, null);

        System.out.println("File downloaded to S3.");

        System.out.println("Extracting phrases with Amazon Textract...");

        //AmazonTextractClient client = new AmazonTextractClient();

        System.out.println("Done.");
    }
}
