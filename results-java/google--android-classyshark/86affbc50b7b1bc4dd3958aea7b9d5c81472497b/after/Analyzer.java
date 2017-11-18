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

package com.google.classyshark.treewalker.methodsperpackage;

import com.google.classyshark.reducer.ArchiveFileReader;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Analyzer {
    public Node analyze(File file ){
        Node rootNode = new Node(file.getName());

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.getName().endsWith(".dex")) {
                    continue;
                }

                System.out.println("Parsing " + zipEntry.getName());
                File tempFile = File.createTempFile("classyshark", "dex");
                try (FileOutputStream fout = new FileOutputStream(tempFile)) {
                    int read;
                    while ((read = zipInputStream.read(buffer)) > 0) {
                        fout.write(buffer, 0, read);
                    }
                }

                try {
                    DexFile dxFile = ArchiveFileReader.loadDexFile(tempFile);

                    Set<? extends ClassDef> classSet = dxFile.getClasses();
                    for (ClassDef o: classSet) {
                        int methodCount = 0;
                        for (Method method : o.getMethods()) {
                            methodCount++;
                        }

                        String translatedClassName = o.getType().replaceAll("\\/", "\\.").substring(1, o.getType().length() - 1);
                        ClassInfo classInfo = new ClassInfo(translatedClassName, methodCount);
                        rootNode.add(classInfo);
                    }

                } catch (Exception ex) {
                    System.err.println("Error parsing Dexfile: " + zipEntry.getName() + ": " + ex.getMessage());
                    ex.printStackTrace(System.err);
                }

            }
        } catch (IOException ex) {
            System.err.println("Error reading file: " + file + ". " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
        return rootNode;
    }

    public Node analyze(String fileName) {
        return analyze(new File(fileName));
    }
}