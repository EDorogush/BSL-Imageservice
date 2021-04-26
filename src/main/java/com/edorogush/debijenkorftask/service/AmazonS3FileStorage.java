package com.edorogush.debijenkorftask.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.edorogush.debijenkorftask.exception.NotFoundException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;

/**
 * The class is implementation of {@link FileStorage} interface and manages CRUD operations for
 * Amazon S3 file storage within selected bucket.
 */
@Service
public class AmazonS3FileStorage implements FileStorage {
  private static final Logger logger = LogManager.getLogger();
  private final int retryDelay;
  private final int maxRetries;
  private final AmazonS3 amazonS3;
  private final String bucketName;

  public AmazonS3FileStorage(
      AmazonS3 amazonS3,
      @Value("${fileStorage.amazonS3.bucket}") String bucketName,
      @Value("${fileStorage.retry.await-before-retry-ms}") int delay,
      @Value("${fileStorage.retry.max-attempts}") int maxAttempts) {
    this.amazonS3 = amazonS3;
    this.bucketName = bucketName;
    this.retryDelay = delay;
    this.maxRetries = maxAttempts - 1;
  }

  @Override
  public Resource get(String fileName) {
    try (S3Object object = amazonS3.getObject(this.bucketName, fileName)) {
      return new ByteArrayResource(object.getObjectContent().readAllBytes());
    } catch (AmazonS3Exception e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
        throw new NotFoundException(String.format("File with name %s not found.", fileName));
      }
      throw e;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * {@inheritDoc} If attempt to create item is failed not due to client error, retry attempt will
   * be done. Retry policy is configured by {@code this.delay} and {@code this.maxAttempts}
   * parameters.
   *
   * @param resource
   * @param fileName absolute path name of item.
   */
  @Override
  public void put(Resource resource, String fileName) {
    RetryPolicy<Object> retryPolicy = initRetryPolicy(bucketName, fileName);
    try (InputStream inputStream = resource.getInputStream()) {
      ObjectMetadata metadata = buildMetadata(resource);
      Failsafe.with(retryPolicy)
          .get(() -> amazonS3.putObject(bucketName, fileName, inputStream, metadata));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void deleteOne(String fileName) {
    amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
  }

  @Override
  public void deleteMany(List<String> fileNames) {
    final DeleteObjectsRequest multiObjectDeleteRequest = buildDeleteObjectsRequest(fileNames);
    amazonS3.deleteObjects(multiObjectDeleteRequest);
  }

  private ObjectMetadata buildMetadata(Resource file) throws IOException {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.contentLength());
    return metadata;
  }

  private DeleteObjectsRequest buildDeleteObjectsRequest(List<String> fileNames) {
    return new DeleteObjectsRequest(bucketName)
        .withKeys(fileNames.toArray(String[]::new))
        .withQuiet(true);
  }

  private RetryPolicy<Object> initRetryPolicy(String bucketName, String fileName) {
    return new RetryPolicy<>()
        .handleIf(ex -> !(isClientError(ex)))
        .onRetry(
            t ->
                logger.info(
                    "Attempt to perform request {} : {} failed. Current attempt number: {}.",
                    HttpMethod.PUT.name(),
                    bucketName + "/" + fileName,
                    t.getAttemptCount(),
                    t.getLastFailure()))
        .withDelay(Duration.ofMillis(this.retryDelay))
        .withMaxRetries(this.maxRetries);
  }

  private boolean isClientError(Throwable throwable) {
    if (!(throwable instanceof AmazonServiceException)) {
      return false;
    }
    final AmazonServiceException serviceException = (AmazonServiceException) throwable;
    final int statusCode = serviceException.getStatusCode();
    return statusCode >= 400 && statusCode < 500;
  }
}
