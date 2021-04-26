package com.edorogush.debijenkorftask.controller;

import com.edorogush.debijenkorftask.exception.BadRequestException;
import com.edorogush.debijenkorftask.exception.ImageLoaderRequestException;
import com.edorogush.debijenkorftask.exception.ImageTypeNotExistException;
import com.edorogush.debijenkorftask.exception.NotFoundException;
import com.edorogush.debijenkorftask.model.ImageType;
import com.edorogush.debijenkorftask.service.ImageService;
import com.edorogush.debijenkorftask.service.RequestValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** This is a MockMVC test covering {@link ImageController}. */
@WebMvcTest(ImageController.class)
@ActiveProfiles("dev")
class ImageControllerTest {
  private static final String GET_REQUEST = "/image/show/{typeName}?reference={imageName}";
  private static final String DELETE_REQUEST = "/image/flush/{typeName}?reference={imageName}";
  private static final String IMAGE_NAME = "abc.jpg";
  private static final ImageType IMAGE_TYPE = ImageType.THUMBNAIL;

  @MockBean private ImageService imageServiceMock;
  @MockBean private RequestValidator requestValidatorMock;

  @Autowired private MockMvc mockMvc;

  @Test
  void searchByName_whenIsPresent_ThenReturnOK() throws Exception {
    final String typeName = ImageType.THUMBNAIL.getName();
    final Resource imageExpected = new ByteArrayResource(new byte[] {});
    when(imageServiceMock.findByName(IMAGE_TYPE.getName(), typeName)).thenReturn(imageExpected);
    doNothing().when(requestValidatorMock).checkImageName(IMAGE_TYPE.getName());
    doNothing().when(requestValidatorMock).checkImageTypeForSearch(typeName);

    mockMvc
        .perform(get(GET_REQUEST, typeName, IMAGE_NAME))
        .andExpect(status().isOk())
        .andExpect(content().bytes(imageExpected.getInputStream().readAllBytes()))
        .andExpect(content().contentType(IMAGE_TYPE.getImageExtension().getMediaType()));
  }

  @Test
  void searchByName_whenNotFound_thenReturn404() throws Exception {
    when(imageServiceMock.findByName(IMAGE_NAME, IMAGE_TYPE.getName()))
        .thenThrow(NotFoundException.class);
    doNothing().when(requestValidatorMock).checkImageName(IMAGE_NAME);
    doNothing().when(requestValidatorMock).checkImageTypeForSearch(IMAGE_TYPE.getName());

    mockMvc
        .perform(get(GET_REQUEST, IMAGE_TYPE.getName(), IMAGE_NAME))
        .andExpect(status().isNotFound());
  }

  @Test
  void searchByName_whenImageLoaderException_thenReturn404() throws Exception {
    when(imageServiceMock.findByName(IMAGE_NAME, IMAGE_TYPE.getName()))
        .thenThrow(ImageLoaderRequestException.class);
    doNothing().when(requestValidatorMock).checkImageName(IMAGE_NAME);
    doNothing().when(requestValidatorMock).checkImageTypeForSearch(IMAGE_TYPE.getName());

    mockMvc
        .perform(get(GET_REQUEST, IMAGE_TYPE.getName(), IMAGE_NAME))
        .andExpect(status().isNotFound());
  }

  @Test
  void searchByName_whenTypeValidationFailed_thenReturn404() throws Exception {
    doThrow(ImageTypeNotExistException.class)
        .when(requestValidatorMock)
        .checkImageTypeForSearch(IMAGE_TYPE.getName());
    doNothing().when(requestValidatorMock).checkImageName(IMAGE_NAME);
    mockMvc
        .perform(get(GET_REQUEST, IMAGE_TYPE.getName(), IMAGE_NAME))
        .andExpect(status().isNotFound());
  }

  @Test
  void searchByName_whenNameValidationFailed_thenReturn400() throws Exception {
    doThrow(BadRequestException.class).when(requestValidatorMock).checkImageName(IMAGE_NAME);
    doNothing().when(requestValidatorMock).checkImageTypeForSearch(IMAGE_TYPE.getName());
    mockMvc
        .perform(get(GET_REQUEST, IMAGE_TYPE.getName(), IMAGE_NAME))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deleteImage_whenRequestIsCorrect_thenReturns204() throws Exception {
    doNothing().when(requestValidatorMock).checkImageName(IMAGE_NAME);
    doNothing().when(requestValidatorMock).checkImageTypeForDelete(IMAGE_TYPE.getName());
    doNothing().when(imageServiceMock).deleteImage(IMAGE_NAME, IMAGE_TYPE.getName());
    mockMvc
        .perform(delete(DELETE_REQUEST, IMAGE_TYPE.getName(), IMAGE_NAME))
        .andExpect(status().isNoContent());
    verify(imageServiceMock, times(1)).deleteImage(IMAGE_NAME, IMAGE_TYPE.getName());
  }
}
