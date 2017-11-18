/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.runtime.jvm.internal.plugins;

import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.model.ModelRule;
import org.gradle.model.Path;
import org.gradle.runtime.base.BinaryContainer;
import org.gradle.runtime.base.internal.BinaryNamingScheme;
import org.gradle.runtime.base.internal.BinaryNamingSchemeBuilder;
import org.gradle.runtime.jvm.ProjectJvmLibrary;
import org.gradle.runtime.jvm.internal.DefaultProjectJarBinary;
import org.gradle.runtime.jvm.internal.ProjectJarBinaryInternal;
import org.gradle.runtime.jvm.toolchain.JavaToolChain;

import java.io.File;

public class CreateJvmBinaries extends ModelRule {
    private final BinaryNamingSchemeBuilder namingSchemeBuilder;
    private final JavaToolChain toolChain;
    private final File binariesDir;
    private final File classesDir;

    // TODO:DAZ Add a ProjectLayout model that can be input to a rule
    public CreateJvmBinaries(BinaryNamingSchemeBuilder namingSchemeBuilder, JavaToolChain toolChain, File buildDir) {
        this.namingSchemeBuilder = namingSchemeBuilder;
        this.toolChain = toolChain;
        this.binariesDir = new File(buildDir, "jars");
        this.classesDir = new File(buildDir, "classes");
    }

    void createBinaries(BinaryContainer binaries, @Path("jvm.libraries") NamedDomainObjectCollection<ProjectJvmLibrary> libraries) {
        for (ProjectJvmLibrary jvmLibrary : libraries) {
            BinaryNamingScheme namingScheme = namingSchemeBuilder
                    .withComponentName(jvmLibrary.getName())
                    .withTypeString("jar")
                    .build();
            ProjectJarBinaryInternal jarBinary = new DefaultProjectJarBinary(jvmLibrary, namingScheme, toolChain);
            jarBinary.source(jvmLibrary.getSource());
            configureBinaryOutputLocations(jarBinary);
            jvmLibrary.getBinaries().add(jarBinary);
            binaries.add(jarBinary);
        }
    }

    private void configureBinaryOutputLocations(ProjectJarBinaryInternal jarBinary) {
        String outputBaseName = jarBinary.getNamingScheme().getOutputDirectoryBase();
        File outputDir = new File(classesDir, outputBaseName);
        jarBinary.setClassesDir(outputDir);
        jarBinary.setResourcesDir(outputDir);
        jarBinary.setJarFile(new File(binariesDir, String.format("%s/%s.jar", outputBaseName, jarBinary.getLibrary().getName())));
    }
}