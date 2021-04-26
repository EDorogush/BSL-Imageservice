package com.edorogush.debijenkorftask.controller;

import com.edorogush.debijenkorftask.IntegrationTestConfiguration;
import com.edorogush.debijenkorftask.WireMockExtension;
import com.edorogush.debijenkorftask.model.ImageType;
import com.github.tomakehurst.wiremock.matching.BinaryEqualToPattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/** Integration test. Starts the whole app and sends real REST requests. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.main.allow-bean-definition-overriding=true"},
    classes = IntegrationTestConfiguration.class)
@ActiveProfiles("dev")
public class ImageAppRestTest {
  private static final String TEST_IMAGE = "cat.jpeg";
  private static final String ORIGINAL = "original";
  @RegisterExtension static WireMockExtension wireMock = new WireMockExtension();
  @Autowired private TestRestTemplate testRestTemplate;

  @Value("${fileStorage.amazonS3.bucket}")
  String bucketName;

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    addUrlWithPort(registry, "source-root-url");
    addUrlWithPort(registry, "aws-test-endpoint");
  }

  @BeforeAll
  static void beforeAll() throws Exception {
    TestImageHandler.clear();
    TestImageHandler.createImageOptimizedForTests(TEST_IMAGE);
  }

  /**
   * This integration test checks application optimistic scenario when neither processed nor
   * original image found in AWS S3 storage. In this case request to {@code source-root-url} must be
   * done. After receiving image from source, it must be written to S3 storage in original and
   * processed variants.
   */
  @Test
  void testGetImage_TheLongestScenario() throws Exception {
    // given
    final String imageName = "abc/def.jpg";
    final String typeName = ImageType.THUMBNAIL.getName();
    byte[] sourceImage = TestImageHandler.readTestImageFromSource(TEST_IMAGE);
    byte[] resizedImage =
        TestImageHandler.readTestImageFromSource(
            TestImageHandler.resolveImageNewName(TEST_IMAGE, typeName));
    // when
    final String fileLocationInS3 = "/" + bucketName + "/" + typeName + "/abc_/abc_def.jpg";
    final String fileOriginalLocationInS3 = "/" + bucketName + "/" + ORIGINAL + "/abc_/abc_def.jpg";
    final String fileLocationInSource = "/" + imageName;
    stubGetRequestFailed(fileLocationInS3, HttpStatus.NOT_FOUND);
    stubGetRequestFailed(fileOriginalLocationInS3, HttpStatus.NOT_FOUND);
    stubGetRequestReturnData(fileLocationInSource, sourceImage, MediaType.IMAGE_JPEG_VALUE);
    stubPutRequestCreated(fileOriginalLocationInS3, sourceImage);
    stubPutRequestCreated(fileLocationInS3, resizedImage);

    final ResponseEntity<Resource> response =
        testRestTemplate.getForEntity(
            "/image/show/{type}/?reference={imageName}", Resource.class, typeName, imageName);
    // then
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    assertThat(response.getBody(), equalTo(new ByteArrayResource(resizedImage)));
    assertThat(
        response.getHeaders().get("Content-Type"),
        is(List.of(ImageType.THUMBNAIL.getImageExtension().getMediaType().toString())));
  }

  /**
   * This integration test check negative scenario when processed image not found in AWS S3, but
   * original image is found. In that case original image is resized and attempt to store resized
   * image in S3 provided. In the case of non-client error, Not Found request is expected.
   */
  @Test
  void testGetImage_WhenPutRequestFailed_ThenNotFound() throws Exception {
    // given
    final String imageName = "abc/def.jpg";
    final String typeName = ImageType.THUMBNAIL.getName();
    byte[] sourceImage = TestImageHandler.readTestImageFromSource(TEST_IMAGE);
    byte[] resizedImage =
        TestImageHandler.readTestImageFromSource(
            TestImageHandler.resolveImageNewName(TEST_IMAGE, typeName));
    // when
    final String fileLocationInS3 = "/" + bucketName + "/" + typeName + "/abc_/abc_def.jpg";
    final String fileOriginalLocationInS3 = "/" + bucketName + "/" + ORIGINAL + "/abc_/abc_def.jpg";
    stubGetRequestFailed(fileLocationInS3, HttpStatus.NOT_FOUND);
    stubGetRequestReturnData(fileOriginalLocationInS3, sourceImage, MediaType.IMAGE_JPEG_VALUE);
    stubForPutRequestFailed(fileLocationInS3, resizedImage, HttpStatus.INTERNAL_SERVER_ERROR);

    final ResponseEntity<Resource> response =
        testRestTemplate.getForEntity(
            "/image/show/{type}/?reference={imageName}", Resource.class, typeName, imageName);
    assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
  }

  private void stubGetRequestReturnData(String url, byte[] body, String mediaType) {
    stubFor(
        get(urlEqualTo(url))
            .willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", mediaType).withBody(body)));
  }

  private void stubGetRequestFailed(String url, HttpStatus status) {
    stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(status.value())));
  }

  private void stubPutRequestCreated(String url, byte[] content) {
    stubFor(
        put(urlEqualTo(url))
            .withRequestBody(new BinaryEqualToPattern(content))
            .willReturn(aResponse().withStatus(HttpStatus.CREATED.value())));
  }

  private void stubForPutRequestFailed(String url, byte[] content, HttpStatus status) {
    stubFor(
        put(urlEqualTo(url))
            .withRequestBody(new BinaryEqualToPattern(content))
            .willReturn(aResponse().withStatus(status.value())));
  }

  private static void addUrlWithPort(DynamicPropertyRegistry registry, String propName) {
    registry.add(
        propName,
        () -> {
          int port = wireMock.getPort();
          return String.format("http://localhost:%d", port);
        });
  }
}
