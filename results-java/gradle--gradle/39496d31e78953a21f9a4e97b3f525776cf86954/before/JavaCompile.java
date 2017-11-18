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

package org.gradle.api.tasks.compile;

import org.gradle.api.AntBuilder;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.hash.DefaultHasher;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.compile.*;
import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.api.internal.tasks.compile.daemon.CompilerDaemonManager;
import org.gradle.api.internal.tasks.compile.incremental.CompilationSourceDirs;
import org.gradle.api.internal.tasks.compile.incremental.IncrementalCompilationSupport;
import org.gradle.api.internal.tasks.compile.incremental.SourceToNameConverter;
import org.gradle.api.internal.tasks.compile.incremental.analyzer.ClassDependenciesAnalyzer;
import org.gradle.api.internal.tasks.compile.incremental.deps.ClassDependencyInfoSerializer;
import org.gradle.api.internal.tasks.compile.incremental.jar.JarSnapshotCache;
import org.gradle.api.internal.tasks.compile.incremental.jar.JarSnapshotsMaker;
import org.gradle.api.internal.tasks.compile.incremental.jar.JarSnapshotter;
import org.gradle.api.internal.tasks.compile.incremental.recomp.RecompilationSpecProvider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.internal.Factory;
import org.gradle.util.SingleMessageLogger;

import java.io.File;

/**
 * Compiles Java source files.
 *
 * <pre autoTested=''>
 *     apply plugin: 'java'
 *     compileJava {
 *         //enable compilation in a separate daemon process
 *         options.fork = true
 *     }
 * </pre>
 */
public class JavaCompile extends AbstractCompile {
    private File dependencyCacheDir;
    private final CompileOptions compileOptions = new CompileOptions();

    @TaskAction
    protected void compile(IncrementalTaskInputs inputs) {
        if (!compileOptions.isIncremental()) {
            compile();
            return;
        }

        SingleMessageLogger.incubatingFeatureUsed("Incremental java compilation");

        //bunch of services that enable incremental java compilation. Should be pushed out to services/factories.
        ClassDependenciesAnalyzer analyzer = new ClassDependenciesAnalyzer(); //TODO SF needs caching
        JarSnapshotCache jarSnapshotCache = new JarSnapshotCache(new File(getProject().getBuildDir(), "jar-snapshot-cache.bin"));
        JarSnapshotter jarSnapshotter = new JarSnapshotter(new DefaultHasher(), analyzer);
        JarSnapshotsMaker jarSnapshotsMaker = new JarSnapshotsMaker(jarSnapshotCache, jarSnapshotter);
        ClassDependencyInfoSerializer dependencyInfoSerializer = new ClassDependencyInfoSerializer(new File(getProject().getBuildDir(), "class-info.bin"));
        CompilationSourceDirs sourceDirs = new CompilationSourceDirs(source);
        SourceToNameConverter sourceToNameConverter = new SourceToNameConverter(sourceDirs); //can be replaced with converter that parses input source class
        RecompilationSpecProvider recompilationSpecProvider = new RecompilationSpecProvider(sourceToNameConverter, dependencyInfoSerializer, (FileOperations) getProject(), jarSnapshotter, jarSnapshotCache);
        IncrementalCompilationSupport incrementalSupport = new IncrementalCompilationSupport(jarSnapshotsMaker, dependencyInfoSerializer, (FileOperations) getProject(),
                analyzer, createCompiler(), getPath(), recompilationSpecProvider);
        org.gradle.api.internal.tasks.compile.Compiler<JavaCompileSpec> compiler = incrementalSupport.prepareCompiler(inputs, sourceDirs);
        performCompilation(compiler);
    }

    protected void compile() {
        performCompilation(createCompiler());
    }

    private CleaningJavaCompiler createCompiler() {
        Factory<AntBuilder> antBuilderFactory = getServices().getFactory(AntBuilder.class);
        JavaCompilerFactory inProcessCompilerFactory = new InProcessJavaCompilerFactory();
        ProjectInternal projectInternal = (ProjectInternal) getProject();
        CompilerDaemonManager compilerDaemonManager = getServices().get(CompilerDaemonManager.class);
        JavaCompilerFactory defaultCompilerFactory = new DefaultJavaCompilerFactory(projectInternal, inProcessCompilerFactory, compilerDaemonManager);
        DelegatingJavaCompiler javaCompiler = new DelegatingJavaCompiler(defaultCompilerFactory);
        return new CleaningJavaCompiler(javaCompiler, antBuilderFactory, getOutputs());
    }

    private void performCompilation(Compiler<JavaCompileSpec> compiler) {
        DefaultJavaCompileSpec spec = new DefaultJavaCompileSpec();
        spec.setSource(getSource());
        spec.setDestinationDir(getDestinationDir());
        spec.setClasspath(getClasspath());
        spec.setDependencyCacheDir(getDependencyCacheDir());
        spec.setSourceCompatibility(getSourceCompatibility());
        spec.setTargetCompatibility(getTargetCompatibility());
        spec.setCompileOptions(compileOptions);
        WorkResult result = compiler.execute(spec);
        setDidWork(result.getDidWork());
    }

    @OutputDirectory
    public File getDependencyCacheDir() {
        return dependencyCacheDir;
    }

    public void setDependencyCacheDir(File dependencyCacheDir) {
        this.dependencyCacheDir = dependencyCacheDir;
    }

    /**
     * Returns the compilation options.
     *
     * @return The compilation options.
     */
    @Nested
    public CompileOptions getOptions() {
        return compileOptions;
    }
}