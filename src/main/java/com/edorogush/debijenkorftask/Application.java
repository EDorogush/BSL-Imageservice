package com.edorogush.debijenkorftask;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;

/** Main entry point. */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newHttpClient();
  }

  @Bean
  public AmazonS3 amazonS3() {
    return AmazonS3ClientBuilder.standard().build();
  }
}
