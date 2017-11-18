package com.intellij.localvcs.core.storage;

import com.intellij.localvcs.core.LocalVcsTestCase;
import com.intellij.localvcs.core.TestStorage;
import org.junit.Test;

import java.io.IOException;


public class ContentTest extends LocalVcsTestCase {
  @Test
  public void testEqualityAndHash() {
    assertTrue(new Content(null, 1).equals(new Content(null, 1)));

    assertFalse(new Content(null, 1).equals(null));
    assertFalse(new Content(null, 1).equals(new Content(null, 2)));

    assertTrue(new Content(null, 1).hashCode() == new Content(null, 1).hashCode());
    assertTrue(new Content(null, 1).hashCode() != new Content(null, 2).hashCode());
  }

  @Test
  public void testUnavailableIfExceptionOccurs() {
    Storage goodStorage = new TestStorage() {
      @Override
      protected byte[] loadContentData(final int id) throws IOException {
        return new byte[0];
      }
    };

    Storage brokenStorage = new TestStorage() {
      @Override
      protected byte[] loadContentData(int id) throws IOException {
        throw new IOException();
      }
    };

    assertTrue(new Content(goodStorage, 0).isAvailable());
    assertFalse(new Content(brokenStorage, 0).isAvailable());
  }
}