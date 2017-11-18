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

package org.gradle.play.tasks;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.compile.daemon.InProcessCompilerDaemonFactory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.play.internal.CleaningPlayToolCompiler;
import org.gradle.play.internal.twirl.DaemonTwirlCompiler;
import org.gradle.play.internal.twirl.TwirlCompileSpec;
import org.gradle.play.internal.twirl.TwirlCompiler;
import org.gradle.play.internal.twirl.TwirlCompilerVersionedInvocationSpecBuilder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Task for compiling twirl templates
 */
public class TwirlCompile extends SourceTask {

    /**
     * FileCollection presenting the twirl compiler classpath.
     */
    private FileCollection compilerClasspath;

    /**
     * Target directory for the compiled template files.
     */
    private File outputDirectory;

    /**
     * Source directory for the template files.
     * Used to find the relative path of templates.
     */
    private File sourceDirectory;


    void setCompiler(Compiler<TwirlCompileSpec> compiler) {
        this.compiler = compiler;
    }

    private Compiler<TwirlCompileSpec> compiler;
    private TwirlStaleOutputCleaner cleaner;

    @InputFiles
    public FileCollection getCompilerClasspath() {
        return compilerClasspath;
    }

    public void setCompilerClasspath(FileCollection compilerClasspath) {
        this.compilerClasspath = compilerClasspath;
    }

    /**
     * Returns the directory to generate the parser source files into.
     *
     * @return The output directory.
     */
    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Specifies the directory to generate the parser source files into.
     *
     * @param outputDirectory The output directory. Must not be null.
     */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }


    /**
     * Returns the root directory where sources are found.
     * Used to find the relative path of templates.
     *
     * @return The root directory for sources.
     */
    public File getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Specifies the root directory where sources are found.
     * Used to find the relative path of templates.
     *
     * @param sourceDirectory TThe root directory for sources.
     */
    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.setSource(sourceDirectory);
    }

    @TaskAction
    void compile(IncrementalTaskInputs inputs) {
        String compilerVersion = detectCompilerVersion();
        if (!inputs.isIncremental()) {
            if(compiler==null){
                compiler = new CleaningPlayToolCompiler<TwirlCompileSpec>(getCompiler(), getOutputs());
            }
            TwirlCompileSpec spec = generateSpec(getSource().getFiles(), compilerVersion);
            compiler.execute(spec);
        } else {
            final Set<File> sourcesToCompile = new HashSet<File>();
            inputs.outOfDate(new Action<InputFileDetails>() {
                public void execute(InputFileDetails inputFileDetails) {
                    sourcesToCompile.add(inputFileDetails.getFile());
                }
            });

            final Set<File> staleOutputFiles = new HashSet<File>();
            inputs.removed(new Action<InputFileDetails>() {
                public void execute(InputFileDetails inputFileDetails) {
                    staleOutputFiles.add(inputFileDetails.getFile());
                }
            });
            if (cleaner == null) {
                cleaner = new TwirlStaleOutputCleaner(getOutputDirectory());
            }
            cleaner.execute(staleOutputFiles);

            TwirlCompileSpec spec = generateSpec(sourcesToCompile, compilerVersion);
            getCompiler().execute(spec);
        }
    }

    private String detectCompilerVersion() {
        final Pattern versionPattern = Pattern.compile("(templates-compiler|twirl-compiler)_(\\d+\\.\\d+)-(\\d+\\.\\d+\\.\\d).jar");
        for (File file : getCompilerClasspath()) {
            Matcher matcher = versionPattern.matcher(file.getName());
            if(matcher.matches()){
                return matcher.group(3);
            }
        }
        return null;
    }


    /**
     * For now just using InProcessCompilerDaemon.
     *
     * TODO allow forked compiler
     */
    private Compiler<TwirlCompileSpec> getCompiler() {
        if (compiler == null) {
            ProjectInternal projectInternal = (ProjectInternal) getProject();
            InProcessCompilerDaemonFactory inProcessCompilerDaemonFactory = getServices().get(InProcessCompilerDaemonFactory.class);
            TwirlCompiler twirlCompiler = new TwirlCompiler(new TwirlCompilerVersionedInvocationSpecBuilder());
            compiler = new DaemonTwirlCompiler(projectInternal.getProjectDir(), twirlCompiler, inProcessCompilerDaemonFactory, getCompilerClasspath().getFiles());

        }
        return compiler;
    }

    private TwirlCompileSpec generateSpec(Set<File> files, String compilerClassName) {
        return new TwirlCompileSpec(getSourceDirectory(), files, getOutputDirectory(), compilerClassName);
    }

    void setCleaner(TwirlStaleOutputCleaner cleaner) {
        this.cleaner = cleaner;
    }


    private static class TwirlStaleOutputCleaner {
        private final File destinationDir;

        public TwirlStaleOutputCleaner(File destinationDir) {
            this.destinationDir = destinationDir;
        }

        public void execute(Set<File> staleSources) {
            for (File removedInputFile : staleSources) {
                File staleOuputFile = calculateOutputFile(removedInputFile);
                staleOuputFile.delete();
            }
        }

        File calculateOutputFile(File inputFile) {
            String inputFileName = inputFile.getName();
            String[] splits = inputFileName.split("\\.");
            String relativeOutputFilePath = String.format("views/%s/%s.template.scala", splits[2], splits[0]); //TODO: use Twirl library instead?
            return new File(destinationDir, relativeOutputFilePath);
        }
    }
}