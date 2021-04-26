package com.edorogush.debijenkorftask.controller;

import com.edorogush.debijenkorftask.model.ImageType;
import com.edorogush.debijenkorftask.service.ImageService;
import com.edorogush.debijenkorftask.service.RequestValidator;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for {@code /image} endpoints. */
@RestController
@RequestMapping(value = "/image")
public class ImageController {
  private final ImageService imageService;
  private final RequestValidator requestValidator;

  public ImageController(ImageService imageService, RequestValidator requestValidator) {
    this.imageService = imageService;
    this.requestValidator = requestValidator;
  }

  @GetMapping(value = {"/show/{imageType}/{dummySeoName}", "/show/{imageType}"})
  public ResponseEntity<Resource> searchByName(
      @PathVariable("imageType") String imageType,
      @PathVariable(required = false, name = "dummySeoName") String dummySeoName,
      @RequestParam("reference") String imageName) {
    requestValidator.checkImageTypeForSearch(imageType);
    requestValidator.checkImageName(imageName);
    Resource resource = imageService.findByName(imageName, imageType);
    return ResponseEntity.ok()
        .contentType(ImageType.findByName(imageType).getImageExtension().getMediaType())
        .body(resource);
  }

  @DeleteMapping("/flush/{imageType}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteImage(
      @PathVariable("imageType") String imageType, @RequestParam("reference") String imageName) {
    requestValidator.checkImageTypeForDelete(imageType);
    requestValidator.checkImageName(imageName);
    imageService.deleteImage(imageName, imageType);
  }
}
