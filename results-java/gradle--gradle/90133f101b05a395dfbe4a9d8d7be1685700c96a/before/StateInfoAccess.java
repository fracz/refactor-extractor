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

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import static org.gradle.internal.UncheckedException.throwAsUncheckedException;

public class StateInfoAccess {

    private static final int UNKNOWN_PREVIOUS_OWNER = 0;
    private DefaultStateInfoProtocol protocol = new DefaultStateInfoProtocol();

    public void ensureStateInfo(RandomAccessFile lockFileAccess) throws IOException {
        if (lockFileAccess.length() < protocol.getSize()) {
            // File did not exist before locking
            markClean(lockFileAccess, UNKNOWN_PREVIOUS_OWNER);
        }
    }

    public void markClean(RandomAccessFile lockFileAccess, int ownerId) throws IOException {
        writeState(lockFileAccess, ownerId);
    }

    public void markDirty(RandomAccessFile lockFileAccess) throws IOException {
        writeState(lockFileAccess, UNKNOWN_PREVIOUS_OWNER);
    }

    private void writeState(RandomAccessFile lockFileAccess, int ownerId) throws IOException {
        lockFileAccess.seek(0);
        lockFileAccess.writeByte(protocol.getVersion());
        protocol.writeState(lockFileAccess, new StateInfo(ownerId, false));
        assert lockFileAccess.getFilePointer() == protocol.getSize();
    }

    public boolean isIntegral(RandomAccessFile lockFileAccess) throws IOException {
        if (lockFileAccess.length() > 0) {
            lockFileAccess.seek(0);
            return lockFileAccess.readByte() == protocol.getVersion();
        }
        return true;
    }

    private int readPreviousOwnerId(RandomAccessFile lockFileAccess) {
        try {
            lockFileAccess.seek(1); //skip the protocol byte
            return lockFileAccess.readInt();
        } catch (EOFException e) {
            // Process has crashed writing to lock file
            return UNKNOWN_PREVIOUS_OWNER;
        } catch (Exception e) {
            throw throwAsUncheckedException(e);
        }
    }

    public StateInfo readStateInfo(RandomAccessFile lockFileAccess) {
        int id =  readPreviousOwnerId(lockFileAccess);
        return new StateInfo(id, id == UNKNOWN_PREVIOUS_OWNER);
    }

    public FileLock tryLock(RandomAccessFile lockFileAccess, boolean shared) throws IOException {
        return lockFileAccess.getChannel().tryLock(0, protocol.getSize(), shared);
    }

    public int getRegionEnd() {
        return protocol.getSize();
    }
}