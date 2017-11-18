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

package org.jetbrains.jet.cli;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.sampullara.cli.Args;
import org.jetbrains.jet.compiler.CompileEnvironment;
import org.jetbrains.jet.compiler.CompileEnvironmentException;
import org.jetbrains.jet.compiler.CompilerPlugin;
import org.jetbrains.jet.compiler.MessageRenderer;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.resolve.java.CompilerSpecialMode;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import static org.jetbrains.jet.cli.KotlinCompiler.ExitCode.*;

/**
 * @author yole
 * @author alex.tkachman
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class KotlinCompiler {

    public enum ExitCode {
        OK(0),
        COMPILATION_ERROR(1),
        INTERNAL_ERROR(2);

        private final int code;

        private ExitCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static void main(String... args) {
        doMain(new KotlinCompiler(), args);
    }

    /**
     * Useful main for derived command line tools
     */
    public static void doMain(KotlinCompiler compiler, String[] args) {
        try {
            ExitCode rc = compiler.exec(args);
            if (rc != OK) {
                System.err.println("exec() finished with " + rc + " return code");
                System.exit(rc.getCode());
            }
        } catch (CompileEnvironmentException e) {
            System.err.println(e.getMessage());
            System.exit(INTERNAL_ERROR.getCode());
        }
    }

    public ExitCode exec(String... args) {
        return exec(System.out, args);
    }

    public ExitCode exec(PrintStream errStream, String... args) {
        CompilerArguments arguments = createArguments();
        if (!parseArguments(errStream, arguments, args)) {
            return INTERNAL_ERROR;
        }
        return exec(errStream, arguments);
    }

    /**
     * Executes the compiler on the parsed arguments
     */
    public ExitCode exec(PrintStream errStream, CompilerArguments arguments) {
        if (arguments.help) {
            usage(errStream);
            return OK;
        }
        System.setProperty("java.awt.headless", "true");

        MessageRenderer messageRenderer = arguments.tags ? MessageRenderer.TAGS : MessageRenderer.PLAIN;

        if (arguments.version) {
            errStream.println(messageRenderer.render(Severity.INFO, "Kotlin Compiler version " + CompilerVersion.VERSION, null, -1, -1));
        }

        CompilerSpecialMode mode;
        if (arguments.mode == null) {
            mode = CompilerSpecialMode.REGULAR;
        } else if (arguments.mode.equals("jdkHeaders")) {
            mode = CompilerSpecialMode.JDK_HEADERS;
        } else if (arguments.mode.equals("builtins")) {
            mode = CompilerSpecialMode.BUILTINS;
        } else {
            throw new IllegalArgumentException("unknown compiler mode: " + arguments.mode);
        }

        CompileEnvironment environment = new CompileEnvironment(messageRenderer, arguments.verbose, mode);
        try {
            configureEnvironment(environment, arguments, errStream);

            boolean noErrors;
            if (arguments.module != null) {
                noErrors = environment.compileModuleScript(arguments.module, arguments.jar, arguments.outputDir, arguments.includeRuntime);
            }
            else {
                noErrors = environment.compileBunchOfSources(arguments.src, arguments.jar, arguments.outputDir, arguments.includeRuntime);
            }
            return noErrors ? OK : COMPILATION_ERROR;
        }
        catch (Throwable t) {
            errStream.println(messageRenderer.renderException(t));
            return INTERNAL_ERROR;
        }
        finally {
            environment.dispose();
        }
    }

    /**
     * Returns true if the arguments can be parsed correctly
     */
    protected boolean parseArguments(PrintStream errStream, CompilerArguments arguments, String[] args) {
        try {
            Args.parse(arguments, args);
            return true;
        }
        catch (IllegalArgumentException e) {
            usage(errStream);
        }
        catch (Throwable t) {
            // Always use tags
            errStream.println(MessageRenderer.TAGS.renderException(t));
        }
        return false;
    }

    protected void usage(PrintStream target) {
        // We should say something like
        //   Args.usage(target, CompilerArguments.class);
        // but currently cli-parser we are using does not support that
        // a corresponding patch has been sent to the authors
        // For now, we are using this:

        PrintStream oldErr = System.err;
        System.setErr(target);
        try {
            // TODO: use proper argv0
            Args.usage(new CompilerArguments());
        } finally {
            System.setErr(oldErr);
        }
    }

    /**
     * Allow derived classes to add additional command line arguments
     */
    protected CompilerArguments createArguments() {
        return new CompilerArguments();
    }

    /**
     * Strategy method to configure the environment, allowing compiler
     * based tools to customise their own plugins
     */
    protected void configureEnvironment(CompileEnvironment environment, CompilerArguments arguments, PrintStream errStream) {
        environment.setIgnoreErrors(false);
        environment.setErrorStream(errStream);

        // install any compiler plugins
        List<CompilerPlugin> plugins = arguments.getCompilerPlugins();
        if (plugins != null) {
            environment.getEnvironment().getCompilerPlugins().addAll(plugins);
        }

        if (arguments.stdlib != null) {
            environment.setStdlib(arguments.stdlib);
        }

        if (arguments.classpath != null) {
            final Iterable<String> classpath = Splitter.on(File.pathSeparatorChar).split(arguments.classpath);
            environment.addToClasspath(Iterables.toArray(classpath, String.class));
        }
    }
}