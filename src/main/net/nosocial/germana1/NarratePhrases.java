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

import java.io.*;
import java.util.List;

public class NarratePhrases {
    public static final String S3_MP3_PATH_DE = "goethe_de/narrate/a1-phrases-%03d-01-de.mp3";
    public static final String S3_MP3_PATH_EN = "goethe_de/narrate/a1-phrases-%03d-02-en.mp3";
    public static final String S3_MP3_PATH_DE_SLOW = "goethe_de/narrate/a1-phrases-%03d-03-de-slow.mp3";


    public static void main(String[] args) throws FileNotFoundException {
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

        Voice germanVoice = null;
        Voice englishVoice = null;
        for (Voice v : voices) {
            if (v.getId().equals("Daniel") && v.getLanguageCode().equals("de-DE")
                    && v.getGender().equals("Male")
                    && v.getSupportedEngines().contains("neural")) {
                germanVoice = v;
            }
            if (v.getId().equals("Amy") && v.getLanguageCode().equals("en-GB")
                    && v.getGender().equals("Female")
                    && v.getSupportedEngines().contains("neural")) {
                englishVoice = v;
            }
        }

        System.out.println("Will use the German germanVoice: " + germanVoice);
        System.out.println("Will use the English germanVoice: " + englishVoice);
        if (germanVoice == null || englishVoice == null) {
            System.out.println("Voices not found!");
            return;
        }

        File germanFile = new File(TranslatePhrases.LOCAL_PATH_IN);
        File englishFile = new File(TranslatePhrases.LOCAL_PATH_OUT);

        // Read text file line by line
        String[] germanPhrases = new BufferedReader(new FileReader(germanFile)).lines().toArray(String[]::new);
        String[] englishPhrases = new BufferedReader(new FileReader(englishFile)).lines().toArray(String[]::new);

        if (germanPhrases.length != englishPhrases.length) {
            System.out.println("Phrases count mismatch!");
            return;
        }

        for (int i = 0; i < germanPhrases.length; i++) {
            System.out.println("Narrating phrase " + (i + 1) + " of " + germanPhrases.length);

            String germanPhrase = "<speak><mark name=\"sub_start\"/><prosody rate=\"medium\">\n"
                    + quoteForPolly(germanPhrases[i])
                    + "\n<break time=\"5s\"/>\n"
                    + "</prosody><mark name=\"sub_end\"/></speak>";

            String germanPhraseSlow = "<speak><mark name=\"sub_start\"/><prosody rate=\"x-slow\">\n"
                    + quoteForPolly(germanPhrases[i])
                    + "\n<break time=\"5s\"/>\n"
                    + "</prosody><mark name=\"sub_end\"/></speak>";

            String englishPhrase = "<speak><mark name=\"sub_start\"/><prosody rate=\"medium\">\n"
                    + quoteForPolly(englishPhrases[i])
                    + "\n<break time=\"5s\"/>\n"
                    + "</prosody><mark name=\"sub_end\"/></speak>";

            String germanFileName = String.format(S3_MP3_PATH_DE, i + 1);
            synthesizeAudio(polly, germanVoice, germanPhrase, germanFileName);
            synthesizeSpeechMarks(polly, germanVoice, germanPhrase, germanFileName.replace(".mp3", ".json"));

            String germanSlowFileName = String.format(S3_MP3_PATH_DE_SLOW, i + 1);
            synthesizeAudio(polly, germanVoice, germanPhraseSlow, germanSlowFileName);
            synthesizeSpeechMarks(polly, germanVoice, germanPhraseSlow, germanSlowFileName.replace(".mp3", ".json"));

            String englishFileName = String.format(S3_MP3_PATH_EN, i + 1);
            synthesizeAudio(polly, englishVoice, englishPhrase, englishFileName);
            synthesizeSpeechMarks(polly, englishVoice, englishPhrase, englishFileName.replace(".mp3", ".json"));

            if (i >= 2) {
                break;
            }
        }

        System.out.println("Done.");
    }

    private static String quoteForPolly(String text) {
        text = text.replace("\"", "&quot;");
        text = text.replace("'", "&apos;");
        text = text.replaceAll("\\(.+\\)", "");
        return text;
    }

    private static void synthesizeAudio(AmazonPolly polly, Voice voice, String text, String s3Path) {
        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(text).withTextType(TextType.Ssml)
                        .withVoiceId(voice.getId())
                        .withOutputFormat(OutputFormat.Mp3)
                        .withEngine("neural");
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream synthStream = synthRes.getAudioStream();

        System.out.println("Saving audio to S3...");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("eu-west-1")
                .build();
        s3Client.putObject(DownloadWordList.BUCKET_NAME, s3Path, synthStream, null);
    }

    private static void synthesizeSpeechMarks(AmazonPolly polly, Voice voice, String text, String s3Path) {
        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(text).withTextType(TextType.Ssml)
                        .withSpeechMarkTypes(SpeechMarkType.Ssml)
                        .withVoiceId(voice.getId())
                        .withOutputFormat(OutputFormat.Json)
                        .withEngine("neural");
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream synthStream = synthRes.getAudioStream();

        System.out.println("Saving speech marks to S3...");

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("eu-west-1")
                .build();
        s3Client.putObject(DownloadWordList.BUCKET_NAME, s3Path, synthStream, null);
    }
}