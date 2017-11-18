/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.devtools.j2objc.util;

import com.google.common.io.CharStreams;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.file.InputFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Nullable;

/**
 * Utilities for reading {@link com.google.devtools.j2objc.file.InputFile}s.
 *
 * @author Tom Ball, Keith Stanger, Mike Thvedt
 */
public class FileUtil {

  /**
   * Find a {@link com.google.devtools.j2objc.file.InputFile} on the source path,
   * either in a directory or a jar.
   * Returns a file guaranteed to exist, or null.
   */
  @Nullable
  public static InputFile findOnSourcePath(String filename) throws IOException {
    for (String pathEntry : Options.getSourcePathEntries()) {
      if (pathEntry.endsWith(".jar")) {
        JarredInputFile jarFile = new JarredInputFile(pathEntry, filename);
        if (jarFile.exists()) {
          return jarFile;
        }
      } else {
        File f = new File(pathEntry);
        if (f.isDirectory()) {
          RegularInputFile regularFile = new RegularInputFile(
              pathEntry + File.separatorChar + filename, filename);
          if (regularFile.exists()) {
            return regularFile;
          }
        }
      }
    }
    return null;
  }

  public static String readFile(InputFile file) throws IOException {
    return CharStreams.toString(file.openReader());
  }

  private static InputStream streamForFile(String filename) throws IOException {
    File f = new File(filename);
    if (f.exists()) {
      return new FileInputStream(f);
    } else {
      InputStream stream = J2ObjC.class.getResourceAsStream(filename);
      if (stream == null) {
        throw new FileNotFoundException(filename);
      }
      return stream;
    }
  }

  /**
   * Reads the given properties file.
   */
  public static Properties loadProperties(String resourceName) throws IOException {
    return loadProperties(streamForFile(resourceName));
  }

  public static Properties loadProperties(InputStream in) throws IOException {
    try {
      Properties p = new Properties();
      p.load(in);
      return p;
    } finally {
      in.close();
    }
  }

  private FileUtil() {}
}