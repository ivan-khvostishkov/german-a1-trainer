package net.nosocial.germana1;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.InputStream;
import java.util.List;

public class NarratePhrases {
    public static final String MP3_FILE_NAME = "a1-phrases-00.mp3";
    public static final String S3_MP3_PATH = "goethe_de/narrate/" + MP3_FILE_NAME;

    public static void main(String[] args) {
        System.out.println("German A1 Trainer Tool (c) 2023 by NoSocial.Net");

        System.out.println("Narrating phrases with Amazon Polly...");

        // Based on https://docs.aws.amazon.com/polly/latest/dg/examples-java.html

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
                "https://polly.eu-west-1.amazonaws.com", "eu-west-1");
        AmazonPolly polly = AmazonPollyClientBuilder.standard()
                .withEndpointConfiguration(endpoint).build();

        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

        // Synchronously ask Amazon Polly to describe available TTS voices.
        DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
        List<Voice> voices = describeVoicesResult.getVoices();

        Voice voice = null;
        for (Voice v : voices) {
            if (v.getId().equals("Daniel") && v.getLanguageCode().equals("de-DE")
                    && v.getSupportedEngines().contains("neural")) {
                voice = v;
                break;
            }
        }

        System.out.println("Will use the voice: " + voice);
        if (voice == null) {
            System.out.println("Voice not found!");
            return;
        }

        String text = """
                Test
                Test
                Test
                """;

        text = text.replace("\"", "&quot;");
        text = text.replace("'", "&apos;");
        text = text.replaceAll("\\(.+\\)", "");
        text = text.replace("\n", "\n<break time=\"5s\"/>\n");
        text = "<speak><prosody rate=\"x-slow\">\n" + text + "</prosody></speak>";

        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(text).withTextType(TextType.Ssml)
                        .withVoiceId(voice.getId())
                        .withOutputFormat(OutputFormat.Mp3)
                        .withEngine("neural");
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream synthStream = synthRes.getAudioStream();

        // Save to S3
        System.out.println("Saving to S3...");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("eu-west-1")
                .build();
        s3Client.putObject(DownloadWordList.BUCKET_NAME, S3_MP3_PATH, synthStream, null);

        System.out.println("Done.");
    }
}