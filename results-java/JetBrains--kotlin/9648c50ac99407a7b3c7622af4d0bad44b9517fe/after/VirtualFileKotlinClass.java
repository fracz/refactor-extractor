/*
 * Copyright 2010-2014 JetBrains s.r.o.
 *
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

package org.jetbrains.jet.lang.resolve.kotlin;

import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import kotlin.Function2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.resolve.java.JvmClassName;
import org.jetbrains.jet.lang.resolve.kotlin.header.KotlinClassHeader;
import org.jetbrains.jet.utils.UtilsPackage;

import java.io.IOException;

public final class VirtualFileKotlinClass extends FileBasedKotlinClass {
    private final static Logger LOG = Logger.getInstance(FileBasedKotlinClass.class);

    private final VirtualFile file;

    private VirtualFileKotlinClass(@NotNull VirtualFile file, @NotNull JvmClassName className, @NotNull KotlinClassHeader classHeader) {
        super(className, classHeader);
        this.file = file;
    }

    @Nullable
    /* package */ static VirtualFileKotlinClass create(@NotNull final VirtualFile file) {
        assert file.getFileType() == JavaClassFileType.INSTANCE : "Trying to read binary data from a non-class file " + file;
        try {
            return create(file.contentsToByteArray(), new Function2<JvmClassName, KotlinClassHeader, VirtualFileKotlinClass>() {
                @Override
                public VirtualFileKotlinClass invoke(JvmClassName name, KotlinClassHeader header) {
                    return new VirtualFileKotlinClass(file, name, header);
                }
            });
        }
        catch (Throwable e) {
            LOG.warn(renderFileReadingErrorMessage(file));
            return null;
        }
    }

    @NotNull
    public VirtualFile getFile() {
        return file;
    }

    @NotNull
    @Override
    protected byte[] getFileContents() {
        try {
            return file.contentsToByteArray();
        }
        catch (IOException e) {
            LOG.error(renderFileReadingErrorMessage(file), e);
            // Seems to be a bug in IDEA inspection
            //noinspection Contract
            throw UtilsPackage.rethrow(e);
        }
    }

    @NotNull
    private static String renderFileReadingErrorMessage(@NotNull VirtualFile file) {
        return "Could not read file: " + file.getPath() + "; "
               + "size in bytes: " + file.getLength() + "; "
               + "file type: " + file.getFileType().getName();
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VirtualFileKotlinClass && ((VirtualFileKotlinClass) obj).file.equals(file);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + file.toString();
    }
}