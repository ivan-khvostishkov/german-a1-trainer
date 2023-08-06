/**
 * Copyright (c) 2023 Ivan Khvostishkov & NoSocial.Net
 */
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
    public static final String JSON_FILE_NAME = "a1-phrases-00.json";
    public static final String S3_MP3_PATH = "goethe_de/narrate/" + MP3_FILE_NAME;
    public static final String S3_JSON_PATH = "goethe_de/narrate/" + JSON_FILE_NAME;

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
        Voice englishVoice = null;
        for (Voice v : voices) {
            if (v.getId().equals("Daniel") && v.getLanguageCode().equals("de-DE")
                    && v.getGender().equals("Male")
                    && v.getSupportedEngines().contains("neural")) {
                voice = v;
            }
            if (v.getId().equals("Amy") && v.getLanguageCode().equals("en-GB")
                    && v.getGender().equals("Female")
                    && v.getSupportedEngines().contains("neural")) {
                englishVoice = v;
            }
        }

        System.out.println("Will use the German voice: " + voice);
        System.out.println("Will use the English voice: " + englishVoice);
        if (voice == null || englishVoice == null) {
            System.out.println("Voices not found!");
            return;
        }

        System.exit(0);

        String text = """
                Test
                Test
                Test
                """;

        text = text.replace("\"", "&quot;");
        text = text.replace("'", "&apos;");
        text = text.replaceAll("\\(.+\\)", "");
        text = text.replace("\n", "\n<break time=\"5s\"/>\n");
        text = "<speak><mark name=\"sub_start\"/><prosody rate=\"x-slow\">\n"
                + text
                + "</prosody><mark name=\"sub_end\"/></speak>";

        synthesizeGermanAudio(polly, voice, text);
        synthesizeGermanSpeechMarks(polly, voice, text);

        System.out.println("Done.");
    }

    private static void synthesizeGermanAudio(AmazonPolly polly, Voice voice, String text) {
        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(text).withTextType(TextType.Ssml)
                        .withVoiceId(voice.getId())
                        .withOutputFormat(OutputFormat.Mp3)
                        .withEngine("neural");
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream synthStream = synthRes.getAudioStream();

        System.out.println("Saving German audio to S3...");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("eu-west-1")
                .build();
        s3Client.putObject(DownloadWordList.BUCKET_NAME, S3_MP3_PATH, synthStream, null);
    }

    private static void synthesizeGermanSpeechMarks(AmazonPolly polly, Voice voice, String text) {
        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(text).withTextType(TextType.Ssml)
                        .withSpeechMarkTypes(SpeechMarkType.Ssml)
                        .withVoiceId(voice.getId())
                        .withOutputFormat(OutputFormat.Json)
                        .withEngine("neural");
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream synthStream = synthRes.getAudioStream();

        System.out.println("Saving German speech marks to S3...");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("eu-west-1")
                .build();
        s3Client.putObject(DownloadWordList.BUCKET_NAME, S3_JSON_PATH, synthStream, null);
    }
}