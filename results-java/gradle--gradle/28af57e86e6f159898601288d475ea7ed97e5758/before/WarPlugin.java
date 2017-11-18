/*
 * Copyright 2007-2008 the original author or authors.
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

package org.gradle.api.plugins;

import org.apache.ivy.core.module.descriptor.Configuration;
import org.gradle.api.DependencyManager;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.dependencies.Filter;
import org.gradle.api.dependencies.MavenPomGenerator;
import org.gradle.api.internal.project.PluginRegistry;
import org.gradle.api.tasks.ConventionValue;
import org.gradle.api.tasks.bundling.Bundle;
import org.gradle.api.tasks.bundling.War;
import org.gradle.api.tasks.ide.eclipse.EclipseWtp;
import org.gradle.util.GUtil;
import org.gradle.util.WrapUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Hans Dockter
 */
public class WarPlugin implements Plugin {
    public static final String PROVIDED_COMPILE = "providedCompile";
    public static final String PROVIDED_RUNTIME = "providedRuntime";
    public static final String ECLIPSE_WTP = "eclipseWtp";
    public static final int PROVIDED_COMPILE_PRIORITY = JavaPlugin.COMPILE_PRIORITY + 100;
    public static final int PROVIDED_RUNTIME_PRIORITY = JavaPlugin.COMPILE_PRIORITY + 150;

    public void apply(Project project, PluginRegistry pluginRegistry, Map customValues) {
        pluginRegistry.apply(JavaPlugin.class, project, pluginRegistry, customValues);
        project.task(project.getArchivesTaskBaseName() + "_jar").setEnabled(false);
        War war = ((Bundle) project.task("libs")).war();
        configureDependencyManager(project.getDependencies());
        configureEclipse(project, war);
    }

    public void configureDependencyManager(DependencyManager dependencyManager) {
        dependencyManager.addConfiguration(
                new Configuration(PROVIDED_COMPILE, Configuration.Visibility.PRIVATE, null, null, true, null));
        dependencyManager.addConfiguration(
                new Configuration(PROVIDED_RUNTIME, Configuration.Visibility.PRIVATE, null, new String[]{PROVIDED_COMPILE}, true, null));
        dependencyManager.addConfiguration(new Configuration(JavaPlugin.COMPILE, Configuration.Visibility.PRIVATE, null, new String[]
                {PROVIDED_COMPILE}, false, null));
        dependencyManager.addConfiguration(new Configuration(JavaPlugin.RUNTIME, Configuration.Visibility.PRIVATE, null, new String[]
                {JavaPlugin.COMPILE, PROVIDED_RUNTIME}, true, null));
        configureMavenScopeMappings(dependencyManager.getMaven());
    }

    private void configureMavenScopeMappings(MavenPomGenerator mavenPomGenerator) {
        mavenPomGenerator.setPackaging(MavenPomGenerator.WAR_PACKAGING);
        mavenPomGenerator.getScopeMappings().addMapping(PROVIDED_COMPILE_PRIORITY, PROVIDED_COMPILE, MavenPomGenerator.PROVIDED);
        mavenPomGenerator.getScopeMappings().addMapping(PROVIDED_RUNTIME_PRIORITY, PROVIDED_RUNTIME, MavenPomGenerator.PROVIDED);
    }

    private void configureEclipse(Project project, War war) {
        EclipseWtp eclipseWtp = configureEclipseWtp(project, war);
        project.task(JavaPlugin.ECLIPSE).dependsOn(eclipseWtp);
    }

    private EclipseWtp configureEclipseWtp(final Project project, final War war) {
        final EclipseWtp eclipseWtp = (EclipseWtp) project.createTask(GUtil.map("type", EclipseWtp.class), ECLIPSE_WTP);

        eclipseWtp.conventionMapping(GUtil.map(
                "warResourceMappings", new ConventionValue() {
            public Object getValue(Convention convention, Task task) {
                Map resourceMappings =  WrapUtil.toMap("/WEB-INF/classes",  GUtil.addLists(java(convention).getSrcDirs(), java(convention).getResourceDirs()));
                resourceMappings.put("/", WrapUtil.toList(java(convention).getWebAppDir()));
                return resourceMappings;
            }
        },
                "outputDirectory", new ConventionValue() {
            public Object getValue(Convention convention, Task task) {
                return java(convention).getClassesDir();
            }
        },
                "deployName", new ConventionValue() {
            public Object getValue(Convention convention, Task task) {
                return project.getName();
            }
        },
                "warLibs", new ConventionValue() {
            public Object getValue(Convention convention, Task task) {
                List warLibs = war.dependencies(eclipseWtp.isFailForMissingDependencies(), false);
                if (war.getAdditionalLibFileSets() != null) {
                    warLibs.addAll(war.getAdditionalLibFileSets());
                }
                return warLibs;
            }
        },
                "projectDependencies", new ConventionValue() {
            public Object getValue(Convention convention, Task task) {
                /*
                * todo We return all project dependencies here, not just the one for runtime. We can't use Ivy here, as we
                * request the project dependencies not via a resolve. We would have to filter the project dependencies
                * ourselfes. This is not completely trivial due to configuration inheritance.
                */
                return task.getProject().getDependencies().getDependencies(Filter.PROJECTS_ONLY);
            }
        }));
        return eclipseWtp;
    }

    private JavaPluginConvention java(Convention convention) {
        return (JavaPluginConvention) convention.getPlugins().get("java");
    }

}