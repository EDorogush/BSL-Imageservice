package com.edorogush.debijenkorftask.service;

/** Utility class to provide static method to resolve file location in root directory. */
public final class StorageLocationResolver {
  private static final int FIRST_FOLDER_NAME_SIZE = 4;
  private static final int SECOND_FOLDER_NAME_SIZE = 4;
  private static final String SLASH = "/";

  private StorageLocationResolver() {}

  public static String resolve(String fileName) {
    final String fileNameWithoutExtension = cutFileExtension(fileName);
    final int nameLength = fileNameWithoutExtension.length();
    if (nameLength <= FIRST_FOLDER_NAME_SIZE) {
      return SLASH;
    }
    final String firstFolderName =
        fileNameWithoutExtension.substring(0, FIRST_FOLDER_NAME_SIZE).toLowerCase();
    if (nameLength <= FIRST_FOLDER_NAME_SIZE + SECOND_FOLDER_NAME_SIZE) {
      return SLASH + firstFolderName + SLASH;
    }
    final String secondFolderName =
        fileNameWithoutExtension
            .substring(FIRST_FOLDER_NAME_SIZE, FIRST_FOLDER_NAME_SIZE + SECOND_FOLDER_NAME_SIZE)
            .toLowerCase();
    return SLASH + firstFolderName + SLASH + secondFolderName + SLASH;
  }

  private static String cutFileExtension(String filename) {
    int index = filename.lastIndexOf(".");
    if (index < 0) {
      return filename;
    }
    return filename.substring(0, index);
  }
}
