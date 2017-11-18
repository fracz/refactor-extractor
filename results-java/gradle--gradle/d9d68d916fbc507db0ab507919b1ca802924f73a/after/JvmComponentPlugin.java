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

package org.gradle.jvm.plugins;

import org.gradle.api.*;
import org.gradle.api.tasks.Copy;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.jvm.JarBinarySpec;
import org.gradle.jvm.JvmLibrarySpec;
import org.gradle.jvm.internal.*;
import org.gradle.jvm.internal.toolchain.JavaToolChainInternal;
import org.gradle.jvm.platform.JavaPlatform;
import org.gradle.jvm.platform.internal.DefaultJavaPlatform;
import org.gradle.jvm.tasks.Jar;
import org.gradle.jvm.toolchain.JavaToolChainRegistry;
import org.gradle.jvm.toolchain.internal.DefaultJavaToolChainRegistry;
import org.gradle.model.*;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.model.internal.type.ModelType;
import org.gradle.platform.base.*;
import org.gradle.platform.base.internal.*;
import org.gradle.util.CollectionUtils;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.capitalize;

/**
 * Base plugin for JVM component support. Applies the
 * {@link org.gradle.language.base.plugins.ComponentModelBasePlugin}.
 * Registers the {@link JvmLibrarySpec} library type for the components container.
 */
@Incubating
public class JvmComponentPlugin implements Plugin<Project> {

    private final ModelRegistry modelRegistry;

    @Inject
    public JvmComponentPlugin(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    @Override
    public void apply(Project project) {
        modelRegistry.getRoot().applyToAllLinksTransitive(ModelType.of(ComponentSpec.class), JarBinaryRules.class);
    }

    @SuppressWarnings("UnusedDeclaration")
    static class Rules extends RuleSource {
        @ComponentType
        public void register(ComponentTypeBuilder<JvmLibrarySpec> builder) {
            builder.defaultImplementation(DefaultJvmLibrarySpec.class);
            builder.internalView(JvmLibrarySpecInternal.class);
        }

        @BinaryType
        public void registerJar(BinaryTypeBuilder<JarBinarySpec> builder) {
            builder.defaultImplementation(DefaultJarBinarySpec.class);
            builder.internalView(JarBinarySpecInternal.class);
        }

        @Model
        public BinaryNamingSchemeBuilder binaryNamingSchemeBuilder() {
            return new DefaultBinaryNamingSchemeBuilder();
        }

        @Model
        public JavaToolChainRegistry javaToolChain(ServiceRegistry serviceRegistry) {
            JavaToolChainInternal toolChain = serviceRegistry.get(JavaToolChainInternal.class);
            return new DefaultJavaToolChainRegistry(toolChain);
        }

        @Model
        public BuildDirHolder buildDirHolder(@Path("buildDir") File buildDir) {
            return new BuildDirHolder(buildDir);
        }

        @Mutate
        public void registerPlatformResolver(PlatformResolvers platformResolvers) {
            platformResolvers.register(new JavaPlatformResolver());
        }

        @ComponentBinaries
        public void createBinaries(ModelMap<JarBinarySpec> binaries, BinaryNamingSchemeBuilder namingSchemeBuilder,
                                   PlatformResolvers platforms, final JvmLibrarySpec jvmLibrary) {
            List<JavaPlatform> selectedPlatforms = resolvePlatforms(platforms, jvmLibrary);
            final Set<String> exportedPackages = jvmLibrary.getExportedPackages();
            for (final JavaPlatform platform : selectedPlatforms) {
                String binaryName = buildBinaryName(jvmLibrary, namingSchemeBuilder, selectedPlatforms, platform);
                binaries.create(binaryName, new Action<JarBinarySpec>() {
                    @Override
                    public void execute(JarBinarySpec jarBinary) {
                        jarBinary.setTargetPlatform(platform);
                        jarBinary.setExportedPackages(exportedPackages);
                    }
                });
            }
        }

        private List<JavaPlatform> resolvePlatforms(final PlatformResolvers platformResolver,
                                                    JvmLibrarySpec jvmLibrarySpec) {
            List<PlatformRequirement> targetPlatforms =
                ((JvmLibrarySpecInternal) jvmLibrarySpec).getTargetPlatforms();
            if (targetPlatforms.isEmpty()) {
                targetPlatforms = Collections.singletonList(
                    DefaultPlatformRequirement.create(DefaultJavaPlatform.current().getName()));
            }
            return CollectionUtils.collect(targetPlatforms, new Transformer<JavaPlatform, PlatformRequirement>() {
                @Override
                public JavaPlatform transform(PlatformRequirement platformRequirement) {
                    return platformResolver.resolve(JavaPlatform.class, platformRequirement);
                }
            });
        }

        private String buildBinaryName(JvmLibrarySpec jvmLibrary, BinaryNamingSchemeBuilder namingSchemeBuilder,
                                       List<JavaPlatform> selectedPlatforms, JavaPlatform platform) {
            BinaryNamingSchemeBuilder componentBuilder = namingSchemeBuilder
                .withComponentName(jvmLibrary.getName())
                .withTypeString("jar");
            if (selectedPlatforms.size() > 1) {
                componentBuilder = componentBuilder.withVariantDimension(platform.getName());
            }
            return componentBuilder.build().getLifecycleTaskName();
        }

        @BinaryTasks
        public void createTasks(ModelMap<Task> tasks, final JarBinarySpec binary) {
            final String jarArchiveName = binary.getJarFile().getName();

            String runtimeJarName = binary.getName();
            final File runtimeClassesDir = binary.getClassesDir();
            final File runtimeJarDestDir = binary.getJarFile().getParentFile();
            final String createRuntimeJar = "create" + capitalize(binary.getName());
            tasks.create(createRuntimeJar, Jar.class, new Action<Jar>() {
                @Override
                public void execute(Jar jar) {
                    jar.setDescription(String.format("Creates the binary file for %s.", binary));
                    jar.from(runtimeClassesDir);
                    jar.from(binary.getResourcesDir());
                    jar.setDestinationDir(runtimeJarDestDir);
                    jar.setArchiveName(jarArchiveName);
                }
            });

            String libName = runtimeJarName.replace("Jar", "");
            String apiJarName = runtimeJarName.replace("Jar", "ApiJar");
            final File apiClassesDir = new File(runtimeClassesDir.getParent(), "apiClasses");
            final String extractApiClasses = "extract" + capitalize(libName + "ApiClasses");
            tasks.create(extractApiClasses, Copy.class, new Action<Copy>() {
                @Override
                public void execute(Copy copy) {
                    copy.from(runtimeClassesDir);
                    copy.into(apiClassesDir);
                    for (String packageName : binary.getExportedPackages()) {
                        copy.include(packageName.replace('.', '/') + "/**/*");
                    }
                    copy.dependsOn(createRuntimeJar);
                }
            });

            final File apiJarDestDir = new File(runtimeJarDestDir.getParentFile(), apiJarName);
            String createApiJar = "create" + capitalize(apiJarName);
            tasks.create(createApiJar, Jar.class, new Action<Jar>() {
                @Override
                public void execute(Jar jar) {
                    jar.setDescription(String.format("Creates the API binary file for %s.", binary));
                    jar.from(apiClassesDir);
                    jar.setDestinationDir(apiJarDestDir);
                    jar.setArchiveName(jarArchiveName);
                    jar.dependsOn(extractApiClasses);
                }
            });
        }
    }
}