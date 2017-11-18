/*
 * Copyright 2009 the original author or authors.
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
package org.gradle;

import org.gradle.api.internal.ExceptionAnalyser;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.api.logging.StandardOutputListener;
import org.gradle.api.logging.StandardOutputLogging;
import org.gradle.configuration.BuildConfigurer;
import org.gradle.execution.BuildExecuter;
import org.gradle.initialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>{@code GradleLauncher} is the main entry point for embedding Gradle. You use this class to manage a Gradle build,
 * as follows:</p>
 *
 * <ol>
 *
 * <li>Optionally create a {@link StartParameter} instance and configure it with the desired properties. The properties
 * of {@code StartParameter} generally correspond to the command-line options of Gradle. You can use {@link
 * #createStartParameter(String[])} to create a {@link StartParameter} from a set of command-line options.</li>
 *
 * <li>Obtain a {@code GradleLauncher} instance by calling {@link #newInstance}, passing in the {@code StartParameter},
 * or an array of Strings that will be treated as command line arguments.</li>
 *
 * <li>Optionally add one or more listeners to the {@code GradleLauncher}.</li>
 *
 * <li>Call {@link #run} to execute the build. This will return a {@link BuildResult}. Note that if the build fails, the
 * resulting exception will be contained in the {@code BuildResult}.</li>
 *
 * <li>Query the build result. You might want to call {@link BuildResult#rethrowFailure()} to rethrow any build
 * failure.</li>
 *
 * </ol>
 *
 * @author Hans Dockter
 */
public class GradleLauncher {
    private enum Stage {
        Configure, PopulateTaskGraph, Build
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleLauncher.class);
    private static GradleLauncherFactory factory = new DefaultGradleLauncherFactory();

    private final GradleInternal gradle;
    private final SettingsHandler settingsHandler;
    private final IGradlePropertiesLoader gradlePropertiesLoader;
    private final BuildLoader buildLoader;
    private final BuildConfigurer buildConfigurer;
    private final LoggingConfigurer loggingConfigurer;
    private final ExceptionAnalyser exceptionAnalyser;
    private final BuildListener buildListener;
    private final InitScriptHandler initScriptHandler;
    private final Set<StandardOutputListener> stdoutListeners = new LinkedHashSet<StandardOutputListener>();
    private final Set<StandardOutputListener> stderrListeners = new LinkedHashSet<StandardOutputListener>();

    /**
     * Creates a new instance.  Don't call this directly, use {@link #newInstance(StartParameter)} or {@link
     * #newInstance(String[])} instead.  Note that this method is package-protected to discourage it's direct use.
     */
    public GradleLauncher(GradleInternal gradle, InitScriptHandler initScriptHandler, SettingsHandler settingsHandler,
                   IGradlePropertiesLoader gradlePropertiesLoader, BuildLoader buildLoader,
                   BuildConfigurer buildConfigurer, LoggingConfigurer loggingConfigurer, BuildListener buildListener,
                   ExceptionAnalyser exceptionAnalyser) {
        this.gradle = gradle;
        this.initScriptHandler = initScriptHandler;
        this.settingsHandler = settingsHandler;
        this.gradlePropertiesLoader = gradlePropertiesLoader;
        this.buildLoader = buildLoader;
        this.buildConfigurer = buildConfigurer;
        this.loggingConfigurer = loggingConfigurer;
        this.exceptionAnalyser = exceptionAnalyser;
        this.buildListener = buildListener;
    }

    /**
     * <p>Executes the build for this GradleLauncher instance and returns the result. Note that when the build fails,
     * the exception is available using {@link BuildResult#getFailure()}.</p>
     *
     * @return The result. Never returns null.
     */
    public BuildResult run() {
        return doBuild(Stage.Build);
    }

    /**
     * Evaluates the settings and all the projects. The information about available tasks and projects is accessible via
     * the {@link org.gradle.api.invocation.Gradle#getRootProject()} object.
     *
     * @return A BuildResult object. Never returns null.
     */
    public BuildResult getBuildAnalysis() {
        return doBuild(Stage.Configure);
    }

    /**
     * Evaluates the settings and all the projects. The information about available tasks and projects is accessible via
     * the {@link org.gradle.api.invocation.Gradle#getRootProject()} object. Fills the execution plan without running
     * the build. The tasks to be executed tasks are available via {@link org.gradle.api.invocation.Gradle#getTaskGraph()}.
     *
     * @return A BuildResult object. Never returns null.
     */
    public BuildResult getBuildAndRunAnalysis() {
        return doBuild(Stage.PopulateTaskGraph);
    }

    private BuildResult doBuild(Stage upTo) {
        addOutputListeners();
        buildListener.buildStarted(gradle);

        Throwable failure = null;
        try {
            doBuildStages(upTo);
        } catch (Throwable t) {
            failure = exceptionAnalyser.transform(t);
        }
        BuildResult buildResult = new BuildResult(gradle, failure);
        buildListener.buildFinished(buildResult);

        // Switching StandardOutputLogging off is important if the Gradle factory is used to
        // run multiple Gradle builds (each one requiring a new instances of GradleLauncher).
        // Switching it off shouldn't be strictly necessary as StandardOutput capturing should
        // always be closed. But as we expose this functionality to the builds, we can't
        // guarantee this.
        StandardOutputLogging.off();
        removeOutputListeners();
        return buildResult;
    }

    private void removeOutputListeners() {
        for (StandardOutputListener stdoutListener : stdoutListeners) {
            loggingConfigurer.removeStandardOutputListener(stdoutListener);
        }
        for (StandardOutputListener stderrListener : stderrListeners) {
            loggingConfigurer.removeStandardErrorListener(stderrListener);
        }
    }

    private void addOutputListeners() {
        for (StandardOutputListener stdoutListener : stdoutListeners) {
            loggingConfigurer.addStandardOutputListener(stdoutListener);
        }
        for (StandardOutputListener stderrListener : stderrListeners) {
            loggingConfigurer.addStandardErrorListener(stderrListener);
        }
    }

    private void doBuildStages(Stage upTo) {
        // Evaluate init scripts
        initScriptHandler.executeScripts(gradle);

        // Evaluate settings script
        SettingsInternal settings = settingsHandler.findAndLoadSettings(gradle, gradlePropertiesLoader);
        buildListener.settingsEvaluated(settings);
        loggingConfigurer.configure(gradle.getStartParameter().getLogLevel());

        // Load build
        buildLoader.load(settings.getRootProject(), gradle, gradlePropertiesLoader.getGradleProperties());
        buildListener.projectsLoaded(gradle);

        // Configure build
        buildConfigurer.process(gradle.getRootProject());
        buildListener.projectsEvaluated(gradle);

        if (upTo == Stage.Configure) {
            return;
        }

        // Populate task graph
        BuildExecuter executer = gradle.getStartParameter().getBuildExecuter();
        executer.select(gradle);

        if (upTo == Stage.PopulateTaskGraph) {
            return;
        }

        // Execute build
        LOGGER.info(String.format("Starting build for %s.", executer.getDisplayName()));
        executer.execute();

        assert upTo == Stage.Build;
    }

    /**
     * Returns a GradleLauncher instance based on the passed start parameter.
     *
     * @param startParameter The start parameter object the GradleLauncher instance is initialized with
     */
    public static GradleLauncher newInstance(final StartParameter startParameter) {
        return factory.newInstance(startParameter);
    }

    /**
     * Returns a GradleLauncher instance based on the passed command line syntax arguments. Certain command line
     * arguments won't have any effect if you choose this method (e.g. -v, -h). If you want to act upon, you better use
     * {@link #createStartParameter(String[])} in conjunction with {@link #newInstance(String[])}.
     *
     * @param commandLineArgs A String array where each element denotes an entry of the Gradle command line syntax
     */
    public static GradleLauncher newInstance(final String[] commandLineArgs) {
        return factory.newInstance(commandLineArgs);
    }

    /**
     * Returns a StartParameter object out of command line syntax arguments. Every possible command line option has it
     * associated field in the StartParameter object.
     *
     * @param commandLineArgs A String array where each element denotes an entry of the Gradle command line syntax
     */
    public static StartParameter createStartParameter(final String[] commandLineArgs) {
        return factory.createStartParameter(commandLineArgs);
    }

    // This is used for mocking
    public static void injectCustomFactory(GradleLauncherFactory gradleLauncherFactory) {
        factory = gradleLauncherFactory == null ? new DefaultGradleLauncherFactory() : gradleLauncherFactory;
    }

    /**
     * <p>Adds a listener to this build instance. The listener is notified of events which occur during the
     * execution of the build. See {@link org.gradle.api.invocation.Gradle#addListener(Object)} for supported listener
     * types.</p>
     *
     * @param listener The listener to add. Has no effect if the listener has already been added.
     */
    public void addListener(Object listener) {
        gradle.addListener(listener);
    }

    /**
     * Use the given listener. See {@link org.gradle.api.invocation.Gradle#useLogger(Object)} for details.
     *
     * @param logger The logger to use.
     */
    public void useLogger(Object logger) {
        gradle.useLogger(logger);
    }

    /**
     * <p>Adds a {@link StandardOutputListener} to this build instance. The listener is notified of any text written to
     * standard output by Gradle's logging system
     *
     * @param listener The listener to add. Has no effect if the listener has already been added.
     */
    public void addStandardOutputListener(StandardOutputListener listener) {
        stdoutListeners.add(listener);
    }

    /**
     * <p>Adds a {@link StandardOutputListener} to this build instance. The listener is notified of any text written to
     * standard error by Gradle's logging system
     *
     * @param listener The listener to add. Has no effect if the listener has already been added.
     */
    public void addStandardErrorListener(StandardOutputListener listener) {
        stderrListeners.add(listener);
    }
}