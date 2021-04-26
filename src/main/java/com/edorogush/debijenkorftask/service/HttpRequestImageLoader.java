package com.edorogush.debijenkorftask.service;

import com.edorogush.debijenkorftask.exception.ImageLoaderRequestException;
import com.edorogush.debijenkorftask.exception.NotFoundInSourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * The class is implementation of {@link ImageLoader} interface and loading images from web source
 * with Http request. Image source URL is configured by {@code source-root-url} parameter.
 */
@Service
public class HttpRequestImageLoader implements ImageLoader {
  private static final Logger logger = LogManager.getLogger();
  private final HttpClient httpClient;
  private final String baseUri;

  public HttpRequestImageLoader(
      HttpClient httpClient, @Value("${source-root-url}") String baseUrl) {
    this.httpClient = httpClient;
    this.baseUri = baseUrl;
    logger.debug("HttpRequestImageLoader initialized with URL: {}", baseUrl);
  }

  @Override
  public Resource getImageFromSource(String imageId) {
    URI uri = URI.create(baseUri + "/" + imageId);
    HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
    try {
      HttpResponse<byte[]> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
      return retrieveFromResponse(response, request, imageId);
    } catch (IOException | InterruptedException e) {
      throw new ImageLoaderRequestException(
          String.format("Request %s : %s failed.", request.method(), request.uri().getPath()), e);
    }
  }

  private Resource retrieveFromResponse(
      HttpResponse<byte[]> response, HttpRequest request, String imageId) {
    if (response.statusCode() == HttpStatus.OK.value()) {
      return new ByteArrayResource(response.body());
    }
    if (response.statusCode() == HttpStatus.NOT_FOUND.value()) {
      throw new NotFoundInSourceException(
          String.format("file %s not found in source url %s", imageId, baseUri));
    }
    throw new ImageLoaderRequestException(
        String.format(
            "Request %s : %s failed with code %d",
            request.method(), request.uri().getPath(), response.statusCode()));
  }
}
