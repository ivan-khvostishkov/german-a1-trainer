package net.nosocial.germana1;

import com.amazonaws.client.builder.AwsClientBuilder;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import com.amazonaws.services.textract.model.GetDocumentAnalysisResult;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExtractPhrases {
    public static final String FILE_NAME = "a1-phrases.txt";
    public static final String LOCAL_PATH = "out/" + FILE_NAME;
    public static final String S3_PATH = "goethe_de/" + FILE_NAME;

    public static void main(String[] args) {
        System.out.println("German A1 Trainer Tool (c) 2023 by NoSocial.Net");

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
                                        .withName(DownloadWordList.PATH)
                                        .withBucket(DownloadWordList.BUCKET_NAME))));


        String jobId = result.getJobId();
        System.out.println("JobId: " + jobId);

        List<String> phrases = getAllPhrases(client, jobId);

        System.out.println("Found " + phrases.size() + " phrases");

        System.out.println("Saving phrases to " + LOCAL_PATH + "...");
        try {
            savePhrases(phrases, LOCAL_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Uploading phrases to S3...");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("eu-west-1")
                .build();

        s3Client.putObject(DownloadWordList.BUCKET_NAME, S3_PATH, new File(LOCAL_PATH));

        System.out.println("Done.");
    }

    @SuppressWarnings("SameParameterValue")
    private static void savePhrases(List<String> phrases, String fileName) {
        StringBuilder sb = new StringBuilder();
        for (String phrase : phrases) {
            sb.append(phrase).append("\n");
        }
        try (java.io.PrintWriter out = new java.io.PrintWriter(fileName)) {
            out.print(sb);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        int charCount = sb.toString().length();
        System.out.println("Saved " + charCount + " characters to " + fileName);
    }

    private static List<String> getAllPhrases(AmazonTextract client, String jobId) {
        // wait for job to complete
        GetDocumentAnalysisResult getDocResult = null;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (2 * 60 * 1000); // wait max 2 min

        while (System.currentTimeMillis() < endTime) {
            getDocResult = client.getDocumentAnalysis(new GetDocumentAnalysisRequest().withJobId(jobId));
            String jobStatus = getDocResult.getJobStatus();
            System.out.println("Job status: " + jobStatus);
            if (jobStatus.equals("SUCCEEDED")) {
                break;
            }
            try {
                //noinspection BusyWait
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if (getDocResult == null) {
            throw new IllegalStateException("No result");
        }

        List<String> phrases = new ArrayList<>();
        String nextToken;
        do {
            List<String> tokenPhrases = getPhrases(getDocResult);
            phrases.addAll(tokenPhrases);
            nextToken = getDocResult.getNextToken();
            if (nextToken != null) {
                getDocResult = client.getDocumentAnalysis(new GetDocumentAnalysisRequest()
                        .withJobId(jobId).withNextToken(nextToken));
            }
        } while (nextToken != null);
        return phrases;
    }

    private static int page = 0;

    private static List<String> getPhrases(GetDocumentAnalysisResult getDocResult) {
        List<String> result = new ArrayList<>();
        List<Block> blocks = getDocResult.getBlocks();
        for (Block block : blocks) {
            if (block.getBlockType().equals("PAGE")) {
                page++;
            }
            if (block.getBlockType().equals("LINE") && page >= 9 && page <= 27) {
                Geometry geometry = block.getGeometry();
                float left = geometry.getBoundingBox().getLeft();
                float top = geometry.getBoundingBox().getTop();
                float bottom = geometry.getBoundingBox().getTop() + geometry.getBoundingBox().getHeight();
                if (left > 0.39 && top > 0.14 && bottom < 0.93) {
                    result.add(block.getText());
                }
            }
        }
        return result;
    }
}
