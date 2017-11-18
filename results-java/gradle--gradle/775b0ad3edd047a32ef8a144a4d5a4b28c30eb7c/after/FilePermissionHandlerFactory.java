/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.internal.nativeplatform.filesystem;

import com.sun.jna.Native;
import org.gradle.internal.nativeplatform.jna.LibC;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.util.Jvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FilePermissionHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilePermissionHandlerFactory.class);

    public static FilePermissionHandler createDefaultFilePermissionHandler() {
        if (OperatingSystem.current().isWindows()) {
            return new WindowsFilePermissionHandler();
        } else if (Jvm.current().isJava7()) {
            try {
                String jdkFilePermissionclass = "org.gradle.internal.nativeplatform.filesystem.jdk7.PosixJdk7FilePermissionHandler";
                FilePermissionHandler jdk7FilePermissionHandler =
                        (FilePermissionHandler) FilePermissionHandler.class.getClassLoader().loadClass(jdkFilePermissionclass).newInstance();
                return jdk7FilePermissionHandler;
            } catch (Exception e) {
                LOGGER.warn("Unable to load Jdk7FilePermissionHandler", e);
            }
        }
        return createComposedFilePermissionHandler();
    }

    private static FilePermissionHandler createComposedFilePermissionHandler() {
        ComposableFilePermissionHandler.Chmod chmod = createChmod();
        return new ComposableFilePermissionHandler(chmod);
    }

    static ComposableFilePermissionHandler.Chmod createChmod() {
        try {
            LibC libc = (LibC) Native.loadLibrary("c", LibC.class);
            return new LibcChmod(libc);
        } catch (Throwable e) {
            return new EmptyChmod();
        }
    }

    private static class LibcChmod implements ComposableFilePermissionHandler.Chmod {
        private final LibC libc;

        public LibcChmod(LibC libc) {
            this.libc = libc;
        }

        public void chmod(File f, int mode) {
            libc.chmod(f.getAbsolutePath(), mode);
        }
    }

    private static class EmptyChmod implements ComposableFilePermissionHandler.Chmod {
        public void chmod(File f, int mode) throws IOException {
        }
    }
}