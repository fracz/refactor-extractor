/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.metadata;

import java.io.IOException;
import java.util.Map;

/**
 * Parses {@link Metadata}s from binary data.
 */
public interface MetadataParser {

  /**
   * Checks whether the parser supports a given mime type.
   *
   * @param mimeType A subtitle mime type.
   * @return Whether the mime type is supported.
   */
  public boolean canParse(String mimeType);

  /**
   * Parses a map of metadata type to metadata objects from the provided binary data.
   *
   * @param data The raw binary data from which to parse the metadata.
   * @param size The size of the input data.
   * @return A parsed {@link Map} of metadata type to metadata objects.
   * @throws IOException If a problem occurred parsing the data.
   */
  public Map<String, Object> parse(byte[] data, int size)
      throws IOException;

}