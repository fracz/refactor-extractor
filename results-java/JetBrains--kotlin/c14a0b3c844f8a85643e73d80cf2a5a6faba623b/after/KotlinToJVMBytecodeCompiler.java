/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.cli.jvm.compiler;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import jet.Function0;
import jet.modules.AllModules;
import jet.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.analyzer.AnalyzeExhaust;
import org.jetbrains.jet.cli.common.CompilerPlugin;
import org.jetbrains.jet.cli.common.CompilerPluginContext;
import org.jetbrains.jet.cli.common.messages.*;
import org.jetbrains.jet.cli.common.util.CompilerPathUtil;
import org.jetbrains.jet.cli.jvm.JVMConfigurationKeys;
import org.jetbrains.jet.codegen.*;
import org.jetbrains.jet.config.CommonConfigurationKeys;
import org.jetbrains.jet.config.CompilerConfiguration;
import org.jetbrains.jet.lang.BuiltinsScopeExtensionMode;
import org.jetbrains.jet.lang.parsing.JetParserDefinition;
import org.jetbrains.jet.lang.parsing.JetScriptDefinition;
import org.jetbrains.jet.lang.parsing.JetScriptDefinitionProvider;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiUtil;
import org.jetbrains.jet.lang.resolve.AnalyzerScriptParameter;
import org.jetbrains.jet.lang.resolve.java.AnalyzerFacadeForJVM;
import org.jetbrains.jet.lang.resolve.java.JvmAbi;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.plugin.JetMainDetector;
import org.jetbrains.jet.utils.ExceptionUtils;
import org.jetbrains.jet.utils.Progress;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author yole
 * @author abreslav
 * @author alex.tkachman
 */
public class KotlinToJVMBytecodeCompiler {

    private KotlinToJVMBytecodeCompiler() {
    }

    @Nullable
    public static ClassFileFactory compileModule(
            K2JVMCompileEnvironmentConfiguration configuration,
            Module moduleBuilder,
            File directory
    ) {
        if (moduleBuilder.getSourceFiles().isEmpty()) {
            throw new CompileEnvironmentException("No source files where defined");
        }

        // TODO creating environment copy each time - not good. original environment shouldn't be passed at all
        CompilerConfiguration compilerConfiguration = configuration.getEnvironment().getConfiguration().copy();
        for (String sourceFile : moduleBuilder.getSourceFiles()) {
            File source = new File(sourceFile);
            if (!source.isAbsolute()) {
                source = new File(directory, sourceFile);
            }

            if (!source.exists()) {
                throw new CompileEnvironmentException("'" + source + "' does not exist");
            }

            compilerConfiguration.add(CommonConfigurationKeys.SOURCE_ROOTS_KEY, source.getPath());
        }

        for (String classpathRoot : moduleBuilder.getClasspathRoots()) {
            compilerConfiguration.add(JVMConfigurationKeys.CLASSPATH_KEY, new File(classpathRoot));
        }

        for (String annotationsRoot : moduleBuilder.getAnnotationsRoots()) {
            compilerConfiguration.add(JVMConfigurationKeys.ANNOTATIONS_PATH_KEY, new File(annotationsRoot));
        }

        Disposable parentDisposable = new Disposable() {
            @Override
            public void dispose() {
            }
        };
        JetCoreEnvironment environment = null;
        try {
            environment = JetCoreEnvironment.createCoreEnvironmentForJVM(parentDisposable,
                                                                         compilerConfiguration);


            K2JVMCompileEnvironmentConfiguration currentK2JVMConfiguration = new K2JVMCompileEnvironmentConfiguration(
                    environment, configuration.getMessageCollector(), Collections.<AnalyzerScriptParameter>emptyList(),
                    configuration.getBuiltinsScopeExtensionMode(), configuration.isStubs(),
                    configuration.getBuiltinToJavaTypesMapping());
            GenerationState generationState = analyzeAndGenerate(currentK2JVMConfiguration);
            if (generationState == null) {
                return null;
            }
            return generationState.getFactory();
        } finally {
            if (environment != null) {
                Disposer.dispose(parentDisposable);
            }
        }
    }

    public static boolean compileModules(
            K2JVMCompileEnvironmentConfiguration configuration,
            @NotNull List<Module> modules,
            @NotNull File directory,
            @Nullable File jarPath,
            @Nullable File outputDir,
            boolean jarRuntime) {

        for (Module moduleBuilder : modules) {
            ClassFileFactory moduleFactory = compileModule(configuration, moduleBuilder, directory);
            if (moduleFactory == null) {
                return false;
            }
            if (outputDir != null) {
                CompileEnvironmentUtil.writeToOutputDirectory(moduleFactory, outputDir);
            }
            else {
                File path = jarPath != null ? jarPath : new File(directory, moduleBuilder.getModuleName() + ".jar");
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(path);
                    CompileEnvironmentUtil.writeToJar(moduleFactory, outputStream, null, jarRuntime);
                    outputStream.close();
                }
                catch (FileNotFoundException e) {
                    throw new CompileEnvironmentException("Invalid jar path " + path, e);
                }
                catch (IOException e) {
                    throw ExceptionUtils.rethrow(e);
                }
                finally {
                    ExceptionUtils.closeQuietly(outputStream);
                }
            }
        }
        return true;
    }

    @Nullable
    private static FqName findMainClass(@NotNull List<JetFile> files) {
        FqName mainClass = null;
        for (JetFile file : files) {
            if (JetMainDetector.hasMain(file.getDeclarations())) {
                if (mainClass != null) {
                    // more than one main
                    return null;
                }
                FqName fqName = JetPsiUtil.getFQName(file);
                mainClass = fqName.child(Name.identifier(JvmAbi.PACKAGE_CLASS));
            }
        }
        return mainClass;
    }

    public static boolean compileBunchOfSources(
            K2JVMCompileEnvironmentConfiguration configuration,
            @Nullable File jar,
            @Nullable File outputDir,
            boolean includeRuntime
    ) {

        FqName mainClass = findMainClass(configuration.getEnvironment().getSourceFiles());

        GenerationState generationState = analyzeAndGenerate(configuration);
        if (generationState == null) {
            return false;
        }

        try {
            ClassFileFactory factory = generationState.getFactory();
            if (jar != null) {
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(jar);
                    CompileEnvironmentUtil.writeToJar(factory, new FileOutputStream(jar), mainClass, includeRuntime);
                    os.close();
                }
                catch (FileNotFoundException e) {
                    throw new CompileEnvironmentException("Invalid jar path " + jar, e);
                }
                catch (IOException e) {
                    throw ExceptionUtils.rethrow(e);
                }
                finally {
                    ExceptionUtils.closeQuietly(os);
                }
            }
            else if (outputDir != null) {
                CompileEnvironmentUtil.writeToOutputDirectory(factory, outputDir);
            }
            else {
                throw new CompileEnvironmentException("Output directory or jar file is not specified - no files will be saved to the disk");
            }
            return true;
        }
        finally {
            generationState.destroy();
        }
    }

    public static boolean compileAndExecuteScript(
            @NotNull K2JVMCompileEnvironmentConfiguration configuration,
            @NotNull List<String> scriptArgs) {
        Class<?> scriptClass = compileScript(configuration, null);
        if(scriptClass == null)
            return false;

        try {
            scriptClass.getConstructor(String[].class).newInstance(new Object[]{scriptArgs.toArray(new String[0])});
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to evaluate script: " + e, e);
        }
        return true;
    }

    public static Class<?> compileScript(
            @NotNull K2JVMCompileEnvironmentConfiguration configuration, ClassLoader parentLoader) {

        GenerationState generationState = analyzeAndGenerate(configuration);
        if (generationState == null) {
            return null;
        }

        try {
            ClassFileFactory factory = generationState.getFactory();
            try {
                GeneratedClassLoader classLoader = new GeneratedClassLoader(factory, new URLClassLoader(new URL[]{
                        // TODO: add all classpath
                        CompilerPathUtil.getRuntimePath().toURI().toURL()
                },
                        parentLoader == null ? AllModules.class.getClassLoader() : parentLoader));
                JetFile scriptFile = configuration.getEnvironment().getSourceFiles().get(0);
                return classLoader.loadClass(ScriptCodegen.classNameForScript(scriptFile));
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to evaluate script: " + e, e);
            }
        }
        finally {
            generationState.destroy();
        }
    }

    @Nullable
    public static GenerationState analyzeAndGenerate(K2JVMCompileEnvironmentConfiguration configuration) {
        return analyzeAndGenerate(configuration, configuration.isStubs(), configuration.getScript());
    }

    @Nullable
    public static GenerationState analyzeAndGenerate(
            K2JVMCompileEnvironmentConfiguration configuration,
            boolean stubs
    ) {
        return analyzeAndGenerate(configuration, stubs, configuration.isScript()
                                                        ? CommandLineScriptUtils.scriptParameters()
                                                        : Collections.<AnalyzerScriptParameter>emptyList());
    }

    @Nullable
    public static GenerationState analyzeAndGenerate(
            K2JVMCompileEnvironmentConfiguration configuration,
            boolean stubs,
            List<AnalyzerScriptParameter> scriptParameters
    ) {
        AnalyzeExhaust exhaust = analyze(configuration, scriptParameters, stubs);

        if (exhaust == null) {
            return null;
        }

        exhaust.throwIfError();

        return generate(configuration, exhaust, stubs);
    }

    @Nullable
    private static AnalyzeExhaust analyze(
            final K2JVMCompileEnvironmentConfiguration configuration,
            final List<AnalyzerScriptParameter> scriptParameters,
            boolean stubs) {
        final JetCoreEnvironment environment = configuration.getEnvironment();
        AnalyzerWithCompilerReport analyzerWithCompilerReport = new AnalyzerWithCompilerReport(configuration.getMessageCollector());
        final Predicate<PsiFile> filesToAnalyzeCompletely =
                stubs ? Predicates.<PsiFile>alwaysFalse() : Predicates.<PsiFile>alwaysTrue();
        analyzerWithCompilerReport.analyzeAndReport(
                new Function0<AnalyzeExhaust>() {
                    @NotNull
                    @Override
                    public AnalyzeExhaust invoke() {
                        return AnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                                environment.getProject(),
                                environment.getSourceFiles(),
                                scriptParameters,
                                filesToAnalyzeCompletely,
                                configuration.getBuiltinsScopeExtensionMode());
                    }
                }, environment.getSourceFiles()
        );

        return analyzerWithCompilerReport.hasErrors() ? null : analyzerWithCompilerReport.getAnalyzeExhaust();
    }

    @NotNull
    private static GenerationState generate(
            final K2JVMCompileEnvironmentConfiguration configuration,
            AnalyzeExhaust exhaust,
            boolean stubs) {
        JetCoreEnvironment environment = configuration.getEnvironment();
        Project project = environment.getProject();
        Progress backendProgress = new Progress() {
            @Override
            public void log(String message) {
                configuration.getMessageCollector().report(CompilerMessageSeverity.LOGGING, message, CompilerMessageLocation.NO_LOCATION);
            }
        };
        GenerationState generationState = new GenerationState(project, ClassBuilderFactories.binaries(stubs), backendProgress,
                                                              exhaust, environment.getSourceFiles(),
                                                              configuration.getBuiltinToJavaTypesMapping());
        generationState.compileCorrectFiles(CompilationErrorHandler.THROW_EXCEPTION);

        List<CompilerPlugin> plugins = configuration.getCompilerPlugins();
        CompilerPluginContext context = new CompilerPluginContext(project, exhaust.getBindingContext(), environment.getSourceFiles());
        for (CompilerPlugin plugin : plugins) {
            plugin.processFiles(context);
        }
        return generationState;
    }

    public static Class compileScript(
            ClassLoader parentLoader,
            String scriptPath,
            @Nullable List<AnalyzerScriptParameter> scriptParameters,
            @Nullable List<JetScriptDefinition> scriptDefinitions) {
        final MessageRenderer messageRenderer = MessageRenderer.PLAIN;
        PrintingMessageCollector messageCollector = new PrintingMessageCollector(System.err, messageRenderer, false);
        Disposable rootDisposable = CompileEnvironmentUtil.createMockDisposable();
        try {
            CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
            compilerConfiguration.addAll(JVMConfigurationKeys.CLASSPATH_KEY, getClasspath(parentLoader));
            compilerConfiguration.addAll(JVMConfigurationKeys.ANNOTATIONS_PATH_KEY, Collections.singletonList(
                    CompilerPathUtil.getJdkAnnotationsPath()));
            compilerConfiguration.add(CommonConfigurationKeys.SOURCE_ROOTS_KEY, scriptPath);
            compilerConfiguration.addAll(CommonConfigurationKeys.SCRIPT_DEFINITIONS_KEY,
                                         scriptDefinitions != null ? scriptDefinitions : Collections.<JetScriptDefinition>emptyList());

            JetCoreEnvironment environment = JetCoreEnvironment.createCoreEnvironmentForJVM(rootDisposable, compilerConfiguration);
            K2JVMCompileEnvironmentConfiguration configuration = new K2JVMCompileEnvironmentConfiguration(
                    environment, messageCollector, scriptParameters != null ? scriptParameters : Collections.<AnalyzerScriptParameter>emptyList(),
                    BuiltinsScopeExtensionMode.ALL,
                    false,
                    BuiltinToJavaTypesMapping.ENABLED);

            try {
                JetScriptDefinitionProvider.getInstance(environment.getProject()).markFileAsScript(environment.getSourceFiles().get(0));
                return compileScript(configuration, parentLoader);
            }
            catch (CompilationException e) {
                messageCollector.report(CompilerMessageSeverity.EXCEPTION, MessageRenderer.PLAIN.renderException(e),
                                        MessageUtil.psiElementToMessageLocation(e.getElement()));
                return null;
            }
            catch (Throwable t) {
                messageCollector.report(CompilerMessageSeverity.EXCEPTION, MessageRenderer.PLAIN.renderException(t),
                                        CompilerMessageLocation.NO_LOCATION);
                return null;
            }

        }
        finally {
            messageCollector.printToErrStream();
            Disposer.dispose(rootDisposable);
        }
    }

    private static Collection<File> getClasspath(ClassLoader loader) {
        return getClasspath(loader, new LinkedList<File>());
    }

    private static Collection<File> getClasspath(ClassLoader loader, LinkedList<File> files) {
        ClassLoader parent = loader.getParent();
        if(parent != null)
            getClasspath(parent, files);

        if(loader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) loader).getURLs()) {
                String urlFile = url.getFile();
                File file = new File(urlFile);
                if(file.exists() && (file.isDirectory() || file.getName().endsWith(".jar"))) {
                    files.add(file);
                }
            }
        }
        return files;
    }
}