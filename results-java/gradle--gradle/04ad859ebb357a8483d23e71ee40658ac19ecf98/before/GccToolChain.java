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
package org.gradle.nativebinaries.toolchain.internal.gcc;

import org.gradle.api.Transformer;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.internal.Factory;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativebinaries.Platform;
import org.gradle.nativebinaries.internal.*;
import org.gradle.nativebinaries.language.assembler.internal.AssembleSpec;
import org.gradle.nativebinaries.language.c.internal.CCompileSpec;
import org.gradle.nativebinaries.language.cpp.internal.CppCompileSpec;
import org.gradle.nativebinaries.toolchain.Gcc;
import org.gradle.nativebinaries.toolchain.internal.AbstractToolChain;
import org.gradle.nativebinaries.toolchain.internal.CommandLineTool;
import org.gradle.nativebinaries.toolchain.internal.ToolType;
import org.gradle.nativebinaries.toolchain.internal.gcc.version.GccVersionDeterminer;
import org.gradle.process.internal.ExecAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Compiler adapter for GCC.
 */
public class GccToolChain extends AbstractToolChain implements Gcc {

    private static final Logger LOGGER = LoggerFactory.getLogger(GccToolChain.class);

    public static final String DEFAULT_NAME = "gcc";

    private final Factory<ExecAction> execActionFactory;
    private final Transformer<String, File> versionDeterminer;

    private String version;

    public GccToolChain(String name, OperatingSystem operatingSystem, FileResolver fileResolver, Factory<ExecAction> execActionFactory) {
        super(name, operatingSystem, new GccToolRegistry(operatingSystem), fileResolver);
        this.execActionFactory = execActionFactory;
        this.versionDeterminer = new GccVersionDeterminer();

        tools.setExeName(ToolType.CPP_COMPILER, "g++");
        tools.setExeName(ToolType.C_COMPILER, "gcc");
        tools.setExeName(ToolType.ASSEMBLER, "as");
        tools.setExeName(ToolType.LINKER, "g++");
        tools.setExeName(ToolType.STATIC_LIB_ARCHIVER, "ar");
    }

    @Override
    protected String getTypeName() {
        return "GNU G++";
    }

    @Override
    protected void checkAvailable(ToolChainAvailability availability) {
        for (ToolType key : ToolType.values()) {
            availability.mustExist(key.getToolName(), tools.locate(key));
        }
        determineVersion();
        if (version == null) {
            availability.unavailable("Could not determine G++ version");
        }
    }

    private void determineVersion() {
        version = determineVersion(tools.locate(ToolType.CPP_COMPILER));
        if (version == null) {
            LOGGER.info("Did not find {} on system", ToolType.CPP_COMPILER.getToolName());
        } else {
            LOGGER.info("Found {} with version {}", ToolType.CPP_COMPILER.getToolName(), version);
        }
    }

    private String determineVersion(File executable) {
        return executable == null ? null : versionDeterminer.transform(executable);
    }

    public PlatformToolChain target(Platform targetPlatform) {
        return new GccPlatformToolChain(targetPlatform);
    }

    private class GccPlatformToolChain implements PlatformToolChain {
        private final Platform targetPlatform;

        public GccPlatformToolChain(Platform targetPlatform) {
            this.targetPlatform = targetPlatform;
        }

        public <T extends BinaryToolSpec> Compiler<T> createCppCompiler() {
            checkAvailable();
            CommandLineTool<CppCompileSpec> commandLineTool = commandLineTool(ToolType.CPP_COMPILER);
            return (Compiler<T>) new CppCompiler(commandLineTool, canUseCommandFile());
        }

        public <T extends BinaryToolSpec> Compiler<T> createCCompiler() {
            checkAvailable();
            CommandLineTool<CCompileSpec> commandLineTool = commandLineTool(ToolType.C_COMPILER);
            return (Compiler<T>) new CCompiler(commandLineTool, canUseCommandFile());
        }

        public <T extends BinaryToolSpec> Compiler<T> createAssembler() {
            checkAvailable();
            CommandLineTool<AssembleSpec> commandLineTool = commandLineTool(ToolType.ASSEMBLER);
            return (Compiler<T>) new Assembler(commandLineTool);
        }

        public <T extends LinkerSpec> Compiler<T> createLinker() {
            checkAvailable();
            CommandLineTool<LinkerSpec> commandLineTool = commandLineTool(ToolType.LINKER);
            return (Compiler<T>) new GccLinker(commandLineTool, canUseCommandFile());
        }

        public <T extends StaticLibraryArchiverSpec> Compiler<T> createStaticLibraryArchiver() {
            checkAvailable();
            CommandLineTool<StaticLibraryArchiverSpec> commandLineTool = commandLineTool(ToolType.STATIC_LIB_ARCHIVER);
            return (Compiler<T>) new ArStaticLibraryArchiver(commandLineTool);
        }

        private <T extends BinaryToolSpec> CommandLineTool<T> commandLineTool(ToolType key) {
            CommandLineTool<T> commandLineTool = new CommandLineTool<T>(key.getToolName(), tools.locate(key), execActionFactory);
            commandLineTool.withPath(getPaths());
            targetToPlatform(commandLineTool, key);
            return commandLineTool;
        }

        private void targetToPlatform(CommandLineTool tool, ToolType key) {
            switch (key) {
                case CPP_COMPILER:
                case C_COMPILER:
                case LINKER:
                    tool.withArguments(gccSwitches());
                    return;
                case ASSEMBLER:
                    // TODO:DAZ
                case STATIC_LIB_ARCHIVER:
                    // TODO:DAZ
            }
        }

        private List<String> gccSwitches() {
            switch (targetPlatform.getArchitecture()) {
                case I386:
                    return args("-m32", "-v");
                case AMD64:
                    return args("-m64", "-v");
                default:
                    return args();
            }
        }

        private List<String> args(String... values) {
            return Arrays.asList(values);
        }

        public String getOutputType() {
            return String.format("%s-%s-%s", getName(), targetPlatform.getArchitecture().name(), operatingSystem.getName());
        }

        private boolean canUseCommandFile() {
            String[] components = version.split("\\.");
            int majorVersion;
            try {
                majorVersion = Integer.valueOf(components[0]);
            } catch (NumberFormatException e) {
                throw new IllegalStateException(String.format("Unable to determine major g++ version from version number %s.", version), e);
            }
            return majorVersion >= 4;
        }
    }

}