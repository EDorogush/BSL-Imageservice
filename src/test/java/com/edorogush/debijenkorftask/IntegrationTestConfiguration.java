package com.edorogush.debijenkorftask;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class IntegrationTestConfiguration {

  @Bean
  public AmazonS3 amazonS3(@Value("${aws-test-endpoint}") String awsEndpoint) {

    String signingRegion = "us-east-1";
    AwsClientBuilder.EndpointConfiguration endpointConfiguration =
        new AwsClientBuilder.EndpointConfiguration(awsEndpoint, signingRegion);
    String accessKey = "invalid";
    String secretKey = "invalid";
    AWSStaticCredentialsProvider awsStaticCredentialsProvider =
        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(endpointConfiguration)
        .withPathStyleAccessEnabled(true)
        .withChunkedEncodingDisabled(true)
        .withCredentials(awsStaticCredentialsProvider)
        .build();
  }
}
