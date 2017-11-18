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

package org.gradle.nativebinaries.toolchain.internal.msvcpp;

import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.nativebinaries.Platform;
import org.gradle.nativebinaries.toolchain.internal.AbstractToolChain;
import org.gradle.internal.Factory;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativebinaries.internal.*;
import org.gradle.nativebinaries.language.assembler.internal.AssembleSpec;
import org.gradle.nativebinaries.language.c.internal.CCompileSpec;
import org.gradle.nativebinaries.language.cpp.internal.CppCompileSpec;
import org.gradle.nativebinaries.toolchain.VisualCpp;
import org.gradle.nativebinaries.toolchain.internal.CommandLineTool;
import org.gradle.nativebinaries.toolchain.internal.ToolRegistry;
import org.gradle.nativebinaries.toolchain.internal.ToolType;
import org.gradle.process.internal.ExecAction;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VisualCppToolChain extends AbstractToolChain implements VisualCpp {

    public static final String DEFAULT_NAME = "visualCpp";

    private final Factory<ExecAction> execActionFactory;
    private final Map<String, String> environment = new HashMap<String, String>();

    private File installDir;

    public VisualCppToolChain(String name, OperatingSystem operatingSystem, FileResolver fileResolver, Factory<ExecAction> execActionFactory) {
        super(name, operatingSystem, new ToolRegistry(operatingSystem), fileResolver);
        this.execActionFactory = execActionFactory;

        tools.setExeName(ToolType.CPP_COMPILER, "cl.exe");
        tools.setExeName(ToolType.C_COMPILER, "cl.exe");
        tools.setExeName(ToolType.ASSEMBLER, "ml.exe");
        tools.setExeName(ToolType.LINKER, "link.exe");
        tools.setExeName(ToolType.STATIC_LIB_ARCHIVER, "lib.exe");
    }

    @Override
    protected String getTypeName() {
        return "Visual C++";
    }

    @Override
    protected void checkAvailable(ToolChainAvailability availability) {
        if (!operatingSystem.isWindows()) {
            availability.unavailable("Not available on this operating system.");
            return;
        }
        if (installDir != null) {
            VisualStudioInstall install = new VisualStudioInstall(installDir);
            availability.mustExist("Visual Studio installation", install.getVisualStudioDir());
            availability.mustExist("Windows SDK", install.getWindowsSdkDir());
        } else {
            // Locate tools in the path
            for (ToolType key : ToolType.values()) {
                availability.mustExist(key.getToolName(), tools.locate(key));
            }
        }
    }

    public File getInstallDir() {
        return installDir;
    }

    public void setInstallDir(Object installDirPath) {
        this.installDir = resolve(installDirPath);
    }

    public PlatformToolChain target(Platform targetPlatform) {
        checkAvailable();
        // TODO:DAZ Even if installDir == null, we should attempt to locate a VisualStudioInstall to configure for non-default architectures.
        if (installDir != null) {
            VisualStudioInstall install = new VisualStudioInstall(installDir);

            tools.setPath(install.getPathEntries(targetPlatform));
            environment.clear();
            environment.putAll(install.getEnvironment(targetPlatform));
        }
        return new VisualCppPlatformToolChain();
    }

    @Override
    public String getSharedLibraryLinkFileName(String libraryName) {
        return getSharedLibraryName(libraryName).replaceFirst("\\.dll$", ".lib");
    }

    private class VisualCppPlatformToolChain implements PlatformToolChain {
        public <T extends BinaryToolSpec> Compiler<T> createCppCompiler() {
            CommandLineTool<CppCompileSpec> commandLineTool = commandLineTool(ToolType.CPP_COMPILER);
            return (Compiler<T>) new CppCompiler(commandLineTool);
        }

        public <T extends BinaryToolSpec> Compiler<T> createCCompiler() {
            CommandLineTool<CCompileSpec> commandLineTool = commandLineTool(ToolType.C_COMPILER);
            return (Compiler<T>) new CCompiler(commandLineTool);
        }

        public <T extends BinaryToolSpec> Compiler<T> createAssembler() {
            CommandLineTool<AssembleSpec> commandLineTool = commandLineTool(ToolType.ASSEMBLER);
            return (Compiler<T>) new Assembler(commandLineTool);
        }

        public <T extends LinkerSpec> Compiler<T> createLinker() {
            CommandLineTool<LinkerSpec> commandLineTool = commandLineTool(ToolType.LINKER);
            return (Compiler<T>) new LinkExeLinker(commandLineTool);
        }

        public <T extends StaticLibraryArchiverSpec> Compiler<T> createStaticLibraryArchiver() {
            CommandLineTool<StaticLibraryArchiverSpec> commandLineTool = commandLineTool(ToolType.STATIC_LIB_ARCHIVER);
            return (Compiler<T>) new LibExeStaticLibraryArchiver(commandLineTool);
        }

        private <T extends BinaryToolSpec> CommandLineTool<T> commandLineTool(ToolType key) {
            CommandLineTool<T> commandLineTool = new CommandLineTool<T>(key.getToolName(), tools.locate(key), execActionFactory);
            commandLineTool.withPath(tools.getPath());
            commandLineTool.withEnvironment(environment);
            return commandLineTool;
        }

        public String getOutputType() {
            return String.format("%s-%s", getName(), operatingSystem.getName());
        }
    }
}