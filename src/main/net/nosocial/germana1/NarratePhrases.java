package net.nosocial.germana1;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.Voice;
import java.util.List;

public class NarratePhrases {

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
    }
}