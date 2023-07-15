package net.nosocial.germana1;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClient;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import com.amazonaws.services.textract.model.GetDocumentAnalysisResult;

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
        String pdf_key = "goethe_de/A1_SD1_Wortliste_02.pdf";
        s3Client.putObject(bucketName, pdf_key, inputStream, null);

        System.out.println("File downloaded to S3.");

        System.out.println("Extracting phrases with Amazon Textract...");

        // Based on https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/java/example_code/textract/src/main/java/com/amazonaws/samples/AnalyzeDocument.java

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
                "https://textract.eu-west-1.amazonaws.com", "eu-west-1");
        AmazonTextract client = AmazonTextractClientBuilder.standard()
                .withEndpointConfiguration(endpoint).build();


        StartDocumentAnalysisResult result = client.startDocumentAnalysis(
                new StartDocumentAnalysisRequest()
                        .withFeatureTypes("TABLES", "FORMS")
                        .withDocumentLocation(
                                new DocumentLocation().withS3Object(new com.amazonaws.services.textract.model.S3Object()
                                        .withName(pdf_key)
                                        .withBucket(bucketName))));


        String jobId = result.getJobId();
        System.out.println("JobId: " + jobId);

        GetDocumentAnalysisResult getDocResult = null;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (2 * 60 * 1000); // wait max 2 min

        while (System.currentTimeMillis() < endTime) {
            getDocResult = client.getDocumentAnalysis(new GetDocumentAnalysisRequest().withJobId(jobId));
            List<Block> blocks = getDocResult.getBlocks();
            if (blocks != null && blocks.size() > 0) {
                break;
            }
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if (getDocResult == null) {
            throw new IllegalStateException("No result");
        }

        com.amazonaws.services.textract.model.Block block = getDocResult.getBlocks().get(0);
        System.out.println("Block: " + block.getBlockType());

        System.out.println("Done.");
    }
}
