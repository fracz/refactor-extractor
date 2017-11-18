/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.tooling.internal.provider;

import org.gradle.BuildResult;
import org.gradle.GradleLauncher;
import org.gradle.StartParameter;
import org.gradle.api.logging.LogLevel;
import org.gradle.initialization.GradleLauncherAction;
import org.gradle.launcher.exec.InitializationAware;
import org.gradle.logging.ShowStacktrace;

import java.io.File;
import java.io.Serializable;

class ConfiguringBuildAction<T> implements GradleLauncherAction<T>, InitializationAware, Serializable {
    private final LogLevel buildLogLevel;
    private final GradleLauncherAction<T> action;
    private final File projectDirectory;
    private final File gradleUserHomeDir;
    private final Boolean searchUpwards;

    ConfiguringBuildAction(File gradleUserHomeDir, File projectDirectory, Boolean searchUpwards,
                           LogLevel buildLogLevel, GradleLauncherAction<T> action) {
        this.gradleUserHomeDir = gradleUserHomeDir;
        this.projectDirectory = projectDirectory;
        this.searchUpwards = searchUpwards;
        this.buildLogLevel = buildLogLevel;
        this.action = action;
    }

    public void configureStartParameter(StartParameter startParameter) {
        startParameter.setProjectDir(projectDirectory);
        if (gradleUserHomeDir != null) {
            startParameter.setGradleUserHomeDir(gradleUserHomeDir);
        }
        if (searchUpwards != null) {
            startParameter.setSearchUpwards(searchUpwards);
        }
        startParameter.setShowStacktrace(ShowStacktrace.ALWAYS);
        if (action instanceof InitializationAware) {
            InitializationAware initializationAware = (InitializationAware) action;
            initializationAware.configureStartParameter(startParameter);
        }

        startParameter.setLogLevel(buildLogLevel);
    }

    public BuildResult run(GradleLauncher launcher) {
        return action.run(launcher);
    }

    public T getResult() {
        return action.getResult();
    }
}