package com.edorogush.debijenkorftask.controller;

import com.edorogush.debijenkorftask.model.ImageType;
import com.edorogush.debijenkorftask.service.ImageProcessor;
import com.edorogush.debijenkorftask.service.ImageSizeProcessor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is utility class to provide static method for test data preparation. As Image processing
 * depends on {@link ImageType} properties it not recommended to keep processed images for tests.
 * Instead, processed images should be created before running tests. This will allow to compare
 * output data in tests and to be ensure that desired images are used. Processed images are created
 * in /test/resource/resizedImages. Image file's name pattern is next: {imageType}_originalFileName.
 */
final class TestImageHandler {
  private static final Path RESOURCE_PATH = Paths.get("src/test/resources");
  private static final String PROCESSED_IMAGES_DIR = "resizedImages";
  private static final List<String> imageTypes =
      Arrays.stream(ImageType.values()).map(ImageType::getName).collect(Collectors.toList());

  TestImageHandler() {}

  /**
   * Method to clean directory where processed images are located. This method should be called
   * before tests run.
   *
   * @throws IOException if an I/O error occurs.
   */
  public static void clear() throws IOException {
    if (Files.notExists(RESOURCE_PATH.resolve(PROCESSED_IMAGES_DIR))) {
      return;
    }
    Files.walkFileTree(
        RESOURCE_PATH.resolve(PROCESSED_IMAGES_DIR),
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }
        });
  }

  /**
   * Method to read image file {@code fileName} as {@code byte} array.
   *
   * @param fileName the relative file name from /test/resources dir.
   * @return file data as {@code byte} array.
   * @throws IOException if an I/O error occurs.
   */
  public static byte[] readTestImageFromSource(String fileName) throws IOException {
    Path path = RESOURCE_PATH.resolve(fileName);
    return Files.readAllBytes(path);
  }

  /**
   * Method to process original image {@code fileName} with all possible ways are provided in {@link
   * ImageProcessor} and to save result in file system. Image file {@code fileName} must me located
   * in /test/resources dir.
   *
   * @param fileName original image name.
   * @throws IOException if an I/O error occurs.
   */
  public static void createImageOptimizedForTests(String fileName) throws IOException {
    if (Files.notExists(RESOURCE_PATH.resolve(PROCESSED_IMAGES_DIR))) {
      Files.createDirectory(RESOURCE_PATH.resolve(PROCESSED_IMAGES_DIR));
    }
    ImageProcessor imageSizeProcessor = new ImageSizeProcessor();
    Resource imageOriginal = new ByteArrayResource(readTestImageFromSource(fileName));
    for (String imageType : imageTypes) {
      Resource imageOptimized = imageSizeProcessor.processImage(imageOriginal, imageType);
      Path path = RESOURCE_PATH.resolve(resolveImageNewName(fileName, imageType));
      try (InputStream imageOptimizedInputStream = imageOptimized.getInputStream()) {
        Files.write(path, imageOptimizedInputStream.readAllBytes());
      }
    }
  }

  /**
   * Method to get relative file name of processed image.
   *
   * @param fileName original file's name.
   * @param imageType {@link ImageType} name, that defines how image is processed.
   * @return processed image's relative name.
   */
  public static String resolveImageNewName(String fileName, String imageType) {
    return PROCESSED_IMAGES_DIR + "/" + imageType + "_" + fileName;
  }
}
