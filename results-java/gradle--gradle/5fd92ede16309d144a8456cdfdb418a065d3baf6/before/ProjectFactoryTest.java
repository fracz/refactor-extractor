/*
 * Copyright 2007 the original author or authors.
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

package org.gradle.api.internal.project;

import org.gradle.api.Project;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.internal.dependencies.DependencyManagerFactory;
import org.gradle.execution.Dag;
import org.gradle.groovy.scripts.FileScriptSource;
import org.gradle.groovy.scripts.ScriptSource;
import static org.gradle.util.ReflectionEqualsMatcher.*;
import org.gradle.StartParameter;
import static org.hamcrest.Matchers.*;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Hans Dockter
 */
@RunWith(JMock.class)
public class ProjectFactoryTest {
    private final JUnit4Mockery context = new JUnit4Mockery();
    private final ClassLoader buildScriptClassLoader = new URLClassLoader(new URL[0]);
    private final File rootDir = new File("/root");
    private final File projectDir = new File("/project").getAbsoluteFile();
    private DependencyManagerFactory dependencyManagerFactoryMock;
    private ITaskFactory taskFactoryMock;
    private BuildScriptProcessor buildScriptProcessor;
    private PluginRegistry pluginRegistry;
    private IProjectRegistry projectRegistry;
    private Dag taskGraph;
    private ProjectFactory projectFactory;
    private AntBuilderFactory antBuilderFactory;

    @Before
    public void setUp() throws Exception {
        context.setImposteriser(ClassImposteriser.INSTANCE);
        dependencyManagerFactoryMock = context.mock(DependencyManagerFactory.class);
        taskFactoryMock = context.mock(ITaskFactory.class);
        buildScriptProcessor = context.mock(BuildScriptProcessor.class);
        pluginRegistry = context.mock(PluginRegistry.class);
        taskGraph = context.mock(Dag.class);
        projectRegistry = context.mock(IProjectRegistry.class);
        antBuilderFactory = context.mock(AntBuilderFactory.class);

        context.checking(new Expectations() {{
            allowing(dependencyManagerFactoryMock).createDependencyManager(with(any(Project.class)));
            allowing(projectRegistry).addProject(with(any(Project.class)));
        }});

        projectFactory = new ProjectFactory(taskFactoryMock, dependencyManagerFactoryMock, buildScriptProcessor, pluginRegistry,
                new StartParameter(), projectRegistry, taskGraph, null, antBuilderFactory);
    }

    @Test
    public void testConstructsRootProjectWithBuildFile() {
        DefaultProject project = projectFactory.createProject("somename", null, rootDir, buildScriptClassLoader);

        assertEquals("somename", project.getName());
        assertEquals("build.gradle", project.getBuildFileName());
        assertNull(project.getParent());
        assertSame(rootDir, project.getRootDir());
        assertSame(rootDir, project.getProjectDir());
        assertSame(project, project.getRootProject());
        assertSame(antBuilderFactory, project.getAntBuilderFactory());
        assertThat(project.getBuild(), notNullValue());
        assertThat(project.getBuild().getRootProject(), sameInstance((Project) project));
        assertThat(project.getBuild().getTaskGraph(), sameInstance((TaskExecutionGraph) taskGraph));
        checkProjectResources(project);

        ScriptSource expectedScriptSource = new FileScriptSource("build file", new File(rootDir, "build.gradle"));
        assertThat(project.getBuildScriptSource(), reflectionEquals(expectedScriptSource));
    }

    @Test
    public void testConstructsChildProjectWithBuildFile() {

        DefaultProject rootProject = projectFactory.createProject("root", null, rootDir, buildScriptClassLoader);
        DefaultProject parentProject = projectFactory.createProject("parent", rootProject, rootDir, buildScriptClassLoader);

        DefaultProject project = projectFactory.createProject("somename", parentProject, projectDir, buildScriptClassLoader);

        assertEquals("somename", project.getName());
        assertEquals("build.gradle", project.getBuildFileName());
        assertSame(parentProject, project.getParent());
        assertSame(rootDir, project.getRootDir());
        assertSame(projectDir, project.getProjectDir());
        assertSame(rootProject, project.getRootProject());
        assertThat(project.getBuild(), sameInstance(rootProject.getBuild()));
        checkProjectResources(project);

        ScriptSource expectedScriptSource = new FileScriptSource("build file", new File(projectDir, "build.gradle").getAbsoluteFile());
        assertThat(project.getBuildScriptSource(), reflectionEquals(expectedScriptSource));
    }

    @Test
    public void testConstructsRootProjectWithEmbeddedBuildScript() {
        ScriptSource expectedScriptSource = context.mock(ScriptSource.class);

        ProjectFactory projectFactory = new ProjectFactory(taskFactoryMock, dependencyManagerFactoryMock, buildScriptProcessor, pluginRegistry,
                new StartParameter(), projectRegistry, taskGraph, expectedScriptSource, antBuilderFactory);

        DefaultProject project = projectFactory.createProject("somename", null, rootDir, buildScriptClassLoader);

        assertEquals("somename", project.getName());
        assertEquals("build.gradle", project.getBuildFileName());
        assertSame(rootDir, project.getRootDir());
        assertSame(rootDir, project.getProjectDir());
        assertNull(project.getParent());
        assertSame(project, project.getRootProject());
        checkProjectResources(project);
        assertSame(project.getBuildScriptSource(), expectedScriptSource);
    }

    private void checkProjectResources(DefaultProject project) {
        assertSame(taskFactoryMock, project.getTaskFactory());
        assertSame(buildScriptClassLoader, project.getBuildScriptClassLoader());
        assertSame(dependencyManagerFactoryMock, project.getDependencyManagerFactory());
        assertSame(buildScriptProcessor, project.getBuildScriptProcessor());
        assertSame(pluginRegistry, project.getPluginRegistry());
        assertSame(projectRegistry, project.getProjectRegistry());
    }

    @Test
    public void testResetClearsProjectRegistryAndDiscardsBuildInstance() {
        DefaultProject root1 = projectFactory.createProject("root", null, rootDir, buildScriptClassLoader);

        context.checking(new Expectations() {{
            one(projectRegistry).reset();
        }});
        projectFactory.reset();

        DefaultProject root2 = projectFactory.createProject("root", null, rootDir, buildScriptClassLoader);
        assertThat(root1.getBuild(), not(sameInstance(root2.getBuild())));
    }
}