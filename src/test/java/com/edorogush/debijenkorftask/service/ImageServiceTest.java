package com.edorogush.debijenkorftask.service;

import com.edorogush.debijenkorftask.exception.NotFoundException;
import com.edorogush.debijenkorftask.model.ImageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for {@link ImageService}. */
class ImageServiceTest {
  private ImageService imageService;

  @Mock private FileStorage fileStorageMock;

  @Mock ImageProcessor imageProcessorMock;

  @Mock ImageLoader imageLoaderMock;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    imageService = new ImageService(fileStorageMock, imageProcessorMock, imageLoaderMock);
  }

  @Test
  void findById_whenOptimizedExists_thenAlreadyReturns() {
    // given
    final String fileName = "abcdefghi.jpg";
    final String optimizerStyle = ImageType.THUMBNAIL.getName();

    final String imageOptimizedPathExpected = optimizerStyle + "/abcd/efgh/" + fileName;
    final Resource imageExpected = new ByteArrayResource(new byte[] {});
    // when
    when(fileStorageMock.get(imageOptimizedPathExpected)).thenReturn(imageExpected);
    Resource imageActual = imageService.findByName(fileName, optimizerStyle);
    // then
    assertThat(imageActual, is(imageExpected));

    verify(fileStorageMock, times(1)).get(any());
    verify(fileStorageMock, times(0)).put(any(), any());
    verify(imageLoaderMock, times(0)).getImageFromSource(any());
  }

  @Test
  void findById_whenOptimizedNotExists_thenSearchForOriginal() {
    // given
    final String fileName = "abc.jpg";
    final String optimizerStyle = ImageType.THUMBNAIL.getName();

    final String imageOriginalPathExpected = "original/" + fileName;
    final Resource imageOriginalExpected = new ByteArrayResource(new byte[] {});
    final String imageOptimizedPathExpected = optimizerStyle + "/" + fileName;
    final Resource imageOptimizedExpected = new ByteArrayResource(new byte[] {1, 2, 3});
    // when
    when(fileStorageMock.get(imageOptimizedPathExpected)).thenThrow(NotFoundException.class);
    when(fileStorageMock.get(imageOriginalPathExpected)).thenReturn(imageOriginalExpected);
    when(imageProcessorMock.processImage(imageOriginalExpected, optimizerStyle))
        .thenReturn(imageOptimizedExpected);
    Resource imageActual = imageService.findByName(fileName, optimizerStyle);
    // then
    assertThat(imageActual, is(imageOptimizedExpected));
    verify(fileStorageMock, times(2)).get(any());
    verify(fileStorageMock, times(1)).put(imageOptimizedExpected, imageOptimizedPathExpected);
    verify(imageLoaderMock, times(0)).getImageFromSource(any());
  }

  @Test
  void findById_whenOptimizedAndOriginalNotExist_thenSearchInSource() {
    // given
    final String fileName = "abc.jpg";
    final String optimizerStyle = ImageType.THUMBNAIL.getName();

    final String imageOriginalPathExpected = "original/" + fileName;
    final Resource imageOriginalExpected = new ByteArrayResource(new byte[] {});
    final String imageOptimizedPathExpected = optimizerStyle + "/" + fileName;
    final Resource imageOptimizedExpected = new ByteArrayResource(new byte[] {1, 2, 3});
    // when
    when(fileStorageMock.get(imageOptimizedPathExpected)).thenThrow(NotFoundException.class);
    when(fileStorageMock.get(imageOriginalPathExpected)).thenThrow(NotFoundException.class);
    when(imageLoaderMock.getImageFromSource(fileName)).thenReturn(imageOriginalExpected);
    when(imageProcessorMock.processImage(imageOriginalExpected, optimizerStyle))
        .thenReturn(imageOptimizedExpected);
    Resource imageActual = imageService.findByName(fileName, optimizerStyle);
    // then
    assertThat(imageActual, is(imageOptimizedExpected));
    verify(fileStorageMock, times(2)).get(any());
    verify(fileStorageMock, times(1)).put(imageOptimizedExpected, imageOptimizedPathExpected);
    verify(fileStorageMock, times(1)).put(imageOriginalExpected, imageOriginalPathExpected);
    verify(imageLoaderMock, times(1)).getImageFromSource(any());
  }

  @Test
  void findById_whenImageNotExist_thenThrowNotFoundException() {
    // given
    final String fileName = "abc.jpg";
    final String optimizerStyle = ImageType.THUMBNAIL.getName();
    final String imageOriginalPathExpected = "original/" + fileName;
    final String imageOptimizedPathExpected = optimizerStyle + "/" + fileName;
    // when
    when(fileStorageMock.get(imageOptimizedPathExpected)).thenThrow(NotFoundException.class);
    when(fileStorageMock.get(imageOriginalPathExpected)).thenThrow(NotFoundException.class);
    when(imageLoaderMock.getImageFromSource(fileName)).thenThrow(NotFoundException.class);
    // then
    assertThrows(NotFoundException.class, () -> imageService.findByName(fileName, optimizerStyle));
    verify(fileStorageMock, times(2)).get(any());
    verify(fileStorageMock, times(0)).put(any(), any());
    verify(imageLoaderMock, times(1)).getImageFromSource(fileName);
  }

  @Test
  void findById_whenNameContainsSlash_thenSymbolReplaced() {
    // given
    final String fileName = "abc/defg/ijk.jpg";
    final String optimizerStyle = ImageType.THUMBNAIL.getName();
    final String imageOriginalPathExpected = "original/abc_/defg/abc_defg_ijk.jpg";
    final String imageOptimizedPathExpected = optimizerStyle + "/abc_/defg/abc_defg_ijk.jpg";
    final Resource imageOriginalExpected = new ByteArrayResource(new byte[] {});
    final Resource imageOptimizedExpected = new ByteArrayResource(new byte[] {1, 2, 3});
    // when
    when(fileStorageMock.get(imageOptimizedPathExpected)).thenThrow(NotFoundException.class);
    when(fileStorageMock.get(imageOriginalPathExpected)).thenThrow(NotFoundException.class);
    when(imageLoaderMock.getImageFromSource(fileName)).thenReturn(imageOriginalExpected);
    when(imageProcessorMock.processImage(imageOriginalExpected, optimizerStyle))
        .thenReturn(imageOptimizedExpected);
    Resource imageActual = imageService.findByName(fileName, optimizerStyle);
    // then
    assertThat(imageActual, is(imageOptimizedExpected));
    verify(fileStorageMock, times(2)).get(any());
    verify(fileStorageMock, times(1)).put(imageOptimizedExpected, imageOptimizedPathExpected);
    verify(fileStorageMock, times(1)).put(imageOriginalExpected, imageOriginalPathExpected);
    verify(imageLoaderMock, times(1)).getImageFromSource(fileName);
  }

  @Test
  void deleteImage_WhenOriginal_ThenDeleteAllReplicas() {
    // given
    final String fileName = "abc/d.jpg";
    final String optimizerStyle = "original";
    final List<String> filePathsToDeleteExpected = new ArrayList<>();
    filePathsToDeleteExpected.add("pre-def-style1/abc_/abc_d.jpg");
    filePathsToDeleteExpected.add("pre-def-style2/abc_/abc_d.jpg");
    filePathsToDeleteExpected.add("original/abc_/abc_d.jpg");
    // when
    when(imageProcessorMock.imageTypes()).thenReturn(List.of("pre-def-style1", "pre-def-style2"));
    imageService.deleteImage(fileName, optimizerStyle);
    // then
    verify(fileStorageMock, times(1)).deleteMany(filePathsToDeleteExpected);
  }

  @Test
  void deleteImage_WhenOptimizedImage_ThenDeleteOne() {
    // given
    final String fileName = "abc/d.jpg";
    final String optimizerStyle = "pre-def-style1";
    final String filePathsToDeleteExpected = "pre-def-style1/abc_/abc_d.jpg";
    // when
    imageService.deleteImage(fileName, optimizerStyle);
    // then
    verify(fileStorageMock, times(1)).deleteOne(filePathsToDeleteExpected);
  }
}
