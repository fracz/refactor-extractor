/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.ui.viewer;

import com.google.classyshark.reducer.ArchiveReader;
import com.google.classyshark.translator.Translator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PanelUtils {
    private PanelUtils(){}

    public static void generateStubFile(Translator translator) {
        generateStubFile(translator.getClassName(),
                translator.toString());
    }

    public static boolean acceptFile(File f) {
        if (f.isDirectory()) {
            return true;
        } else {
            return ArchiveReader.isSupportedArchiveFile(f);
        }
    }

    public static String getFileChooserDescription() {
        return "dex, jar, apk, class";
    }

    private static void generateStubFile(String className, String classBody) {
        try {
            Files.write(Paths.get("./" + className + ".java"), classBody.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}