package com.edorogush.debijenkorftask.service;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/** Tests for {@link StorageLocationResolver}. */
class StorageLocationResolverTest {

  @Test
  void resolve_whenNameLess4_thenNoFolders() {
    // given
    final String fileName = "abcd.jpg";
    // when
    final String locationActual = StorageLocationResolver.resolve(fileName);
    // then
    final String locationExpected = "/";
    assertThat(locationActual, is(locationExpected));
  }

  @Test
  void resolve_whenNameBetween4And8_thenOneFolder() {
    // given
    final String fileName = "abcdefgh.jpg";
    // when
    final String locationActual = StorageLocationResolver.resolve(fileName);
    // then
    final String locationExpected = "/abcd/";
    assertThat(locationActual, is(locationExpected));
  }

  @Test
  void resolve_whenNameMore8_thenTwoFolders() {
    // given
    final String fileName = "abcdefghij.jpg";
    // when
    final String locationActual4Symbols = StorageLocationResolver.resolve(fileName);
    // then
    final String locationExpected = "/abcd/efgh/";
    assertThat(locationActual4Symbols, is(locationExpected));
  }
}
