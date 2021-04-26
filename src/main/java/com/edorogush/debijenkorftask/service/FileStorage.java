package com.edorogush.debijenkorftask.service;

import com.edorogush.debijenkorftask.exception.NotFoundException;
import org.springframework.core.io.Resource;

import java.util.List;

/** Represents CRUD operations for selected storage. */
public interface FileStorage {
  /**
   * Method to read data item from storage.
   *
   * @param fileName absolute path name of item.
   * @return {@link Resource} item
   * @throws NotFoundException when requested file not found.
   */
  Resource get(String fileName);

  /**
   * Method to create item in storage
   *
   * @param file {@link Resource} item's data.
   * @param fileName absolute path name of item.
   */
  void put(Resource file, String fileName);

  /**
   * Method to delete one item from storage.
   *
   * @param fileName absolute path name of item.
   */
  void deleteOne(String fileName);

  /**
   * Method to delete many items from storage.
   *
   * @param fileNames List of absolute path names of items should be deleted.
   */
  void deleteMany(List<String> fileNames);
}
