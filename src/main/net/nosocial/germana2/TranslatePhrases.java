/**
 * Copyright (c) 2023 Ivan Khvostishkov & NoSocial.Net
 */
package net.nosocial.germana2;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClientBuilder;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;

import java.io.File;
import java.io.IOException;

public class TranslatePhrases {
    /**
     * NOTE: we manually review and edit the extracted phrases to join multi-line phrases lines into a single line
     */
    public static final String EDITED_FILE_NAME = "a2-phrases-edited.txt";
    public static final String LOCAL_PATH_IN = "out/" + EDITED_FILE_NAME;

    public static final String OUTPUT_FILE_NAME = "a2-phrases-english.txt";
    public static final String LOCAL_PATH_OUT = "out/" + OUTPUT_FILE_NAME;
    public static final String S3_PATH = "goethe_de/extract/" + OUTPUT_FILE_NAME;

    public static void main(String[] args) throws IOException {
        System.out.println("German A2 Hands-free Trainer (c) 2023-2024 by NoSocial.Net");

        System.out.println("Translating phrases with Amazon Translate...");

        // Based on the example from: https://docs.aws.amazon.com/translate/latest/dg/examples-java.html .

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
                "https://translate.eu-west-1.amazonaws.com", "eu-west-1");
        AmazonTranslate translate = AmazonTranslateClientBuilder.standard()
                .withEndpointConfiguration(endpoint).build();

        // Translate all phrases from edited file line by line
        // and write to output file.
        try (
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(LOCAL_PATH_IN));
                java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(LOCAL_PATH_OUT))
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                String translatedText = getTranslatedText(translate, line);
                bw.write(translatedText);
                bw.newLine();
            }
            bw.flush();
        }

        // Upload output file to S3
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("eu-west-1")
                .build();

        s3Client.putObject(DownloadWordList.BUCKET_NAME, S3_PATH, new File(LOCAL_PATH_OUT));

        System.out.println("Done.");
    }

    private static String getTranslatedText(AmazonTranslate translate, String textToTranslate) {
        TranslateTextRequest request = new TranslateTextRequest()
                .withText(textToTranslate)
                .withSourceLanguageCode("de")
                .withTargetLanguageCode("en");
        TranslateTextResult result  = translate.translateText(request);
        return result.getTranslatedText();
    }
}