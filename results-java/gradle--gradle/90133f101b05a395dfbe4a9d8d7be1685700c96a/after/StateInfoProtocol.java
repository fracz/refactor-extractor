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
package org.gradle.cache.internal.filelock;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface StateInfoProtocol {

    /**
     * size (bytes) of the data of this protocol
     */
    int getSize();

    /**
     * single byte that describes the version.
     * an implementation protocol should increment the value when protocol changes in an incompatible way
     */
    int getVersion();

    /**
     * writes the state data
     */
    void writeState(RandomAccessFile lockFileAccess, StateInfo stateInfo) throws IOException;

    /**
     * reads the state data
     */
    StateInfo readState(RandomAccessFile lockFileAccess);
}