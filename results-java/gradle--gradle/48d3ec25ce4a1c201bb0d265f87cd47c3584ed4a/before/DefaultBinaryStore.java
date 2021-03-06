/*
 * Copyright 2013 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice.resolveengine.store;

import org.gradle.api.internal.cache.BinaryStore;
import org.gradle.util.GStreamUtil;

import java.io.*;

import static org.gradle.internal.UncheckedException.throwAsUncheckedException;

class DefaultBinaryStore implements BinaryStore {
    private File file;
    private DataOutputStream outputStream;
    private int offset = -1;

    public DefaultBinaryStore(File file) {
        this.file = file;
        try {
            outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        } catch (FileNotFoundException e) {
            throw throwAsUncheckedException(e);
        }
    }

    public void write(WriteAction write) {
        if (offset == -1) {
            this.offset = outputStream.size();
        }
        try {
            write.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Problems writing to " + diagnose(), e);
        }
    }

    public DataInputStream getInput() {
        try {
            return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw throwAsUncheckedException(e);
        }
    }

    public String diagnose() {
        return toString() + " (exist: " + file.exists() + ")";
    }

    @Override
    public String toString() {
        return "Binary store in " + file;
    }

    public BinaryData done() {
        try {
            outputStream.flush();
            return new SimpleBinaryData(file, offset, diagnose());
        } catch (IOException e) {
            throw new RuntimeException("Problems flushing data to " + diagnose(), e);
        } finally {
            offset = -1;
        }
    }

    public void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
            throw throwAsUncheckedException(e);
        }
        file.delete();
    }

    private static class SimpleBinaryData implements BinaryData {
        private DataInputStream input;
        private File inputFile;
        private int offset;
        private final String sourceDescription;

        public SimpleBinaryData(File inputFile, int offset, String sourceDescription) {
            this.inputFile = inputFile;
            this.offset = offset;
            this.sourceDescription = sourceDescription;
        }

        public <T> T read(ReadAction<T> readAction) {
            try {
                if (input == null) {
                    input = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
                    GStreamUtil.skipBytes(offset, input);
                }
                return readAction.read(input);
            } catch (Exception e) {
                throw new RuntimeException("Problems reading data from " + sourceDescription, e);
            }
        }

        public void done() {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Problems closing the " + sourceDescription, e);
            }
            input = null;
        }

        public String toString() {
            return sourceDescription;
        }
    }
}