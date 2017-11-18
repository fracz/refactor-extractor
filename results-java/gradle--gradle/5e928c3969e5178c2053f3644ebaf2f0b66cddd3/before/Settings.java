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

package org.gradle.api.initialization;

import groovy.lang.Closure;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.gradle.StartParameter;
import org.gradle.api.DependencyManager;
import org.gradle.api.Project;
import org.gradle.api.UnknownProjectException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.dependencies.ResolverContainer;
import org.gradle.api.dependencies.Dependency;

import java.io.File;

/**
 * <p><code>Settings</code> declares the configuration required to instantiate and evaluate the hierarchy of {@link
 * Project} instances which are to participate in a build.</p>
 *
 * <p>There is a one-to-one correspondence between a <code>Settings</code> instance and a <code>{@value
 * #DEFAULT_SETTINGS_FILE}</code> settings file. Before Gradle assembles the projects for a build, it creates a
 * <code>Settings</code> instance and executes the settings file against it.</p>
 *
 * <h3>Assembling a Multi-Project Build</h3>
 *
 * <p>One of the purposes of the <code>Settings</code> object is to allow you to declare the projects which are to be
 * included in the build. You add projects to the build using the {@link #include(String[])} method.  There is always a
 * root project included in a build.  It is added automatically when the <code>Settings</code> object is created.  The
 * root project's name defaults to the name of the directory containing the settings file. The root project's project
 * directory defaults to the directory containing the settings file.</p>
 *
 * <p>When a project is included in the build, a {@link ProjectDescriptor} is created. You can use this descriptor to
 * change the default vaules for several properties of the project.</p>
 *
 * <h3>Defining the Build Class-path</h3>
 *
 * <p>Using the <code>Settings</code> object, you can define the classpath which will be used to load the build scripts,
 * and all objects used by them. This should also include the non-standard plugins which the build scripts will
 * use.</p>
 *
 * <h3>Using Settings in a Settings File</h3>
 *
 * <h4>Dynamic Properties</h4>
 *
 * <p>In addition to the properties of this interface, the {@code Settings} object makes some additional read-only
 * properties available to the settings script. This includes properties from the following sources:</p>
 *
 * <ul>
 *
 * <li>Defined in the {@value org.gradle.api.Project#GRADLE_PROPERTIES} file located in the settings directory of the
 * build.</li>
 *
 * <li>Defined the {@value org.gradle.api.Project#GRADLE_PROPERTIES} file located in the user's {@code .gradle}
 * directory.</li>
 *
 * <li>Provided on the command-line using the -P option.</li>
 *
 * </ul>
 *
 * @author Hans Dockter
 */
public interface Settings {
    /**
     * <p>The default name for the settings file.</p>
     */
    String DEFAULT_SETTINGS_FILE = "settings.gradle";

    String BUILD_DEPENDENCIES_PROJECT_GROUP = "org.gradle";
    String BUILD_DEPENDENCIES_PROJECT_VERSION = "SNAPSHOT";
    String BUILD_DEPENDENCIES_PROJECT_NAME = "build";

    /**
     * <p>Adds the given projects to the build. Each path in the supplied list is treated as the path of a project to
     * add to the build. Note that these path are not file paths, but instead specify the location of the new project in
     * the project heirarchy. As such, the supplied paths must use the ':' character as separator.</p>
     *
     * <p>The last element of the supplied path is used as the project name. The supplied path is converted to a project
     * directory relative to the root project directory.</p>
     *
     * <p>As an example, the path {@code a:b} adds a project with path {@code :a:b}, name {@code b} and project
     * directory {@code $rootDir/a/b}.</p>
     *
     * @param projectPaths the projects to add.
     */
    void include(String[] projectPaths);

    /**
     * <p>Adds the given projects to the build. Each name in the supplied list is treated as the name of a project to
     * add to the build.</p>
     *
     * <p>The supplied name is converted to a project directory relative to the <em>parent</em> directory of the root
     * project directory.</p>
     *
     * <p>As an example, the name {@code a} add a project with path {@code :a}, name {@code a} and project directory
     * {@code $rootDir/../a}.</p>
     *
     * @param projectNames the projects to add.
     */
    void includeFlat(String[] projectNames);

    /**
     * <p>Returns the {@link DependencyManager} which manages the classpath to use for the build scripts.</p>
     *
     * @return the dependency manager instance responsible for managing the dependencies for the users build script
     *         classpath.
     */
    DependencyManager getDependencyManager();

    /**
     * <p>Adds dependencies to the build script classpath. See {@link DependencyManager#dependencies(java.util.List,
     * Object[])} for more details.</p>
     *
     * @param dependencies The dependencies to add.
     * @throws InvalidUserDataException When one of the given object cannot be converted to a {@code Dependency}.
     */
    void dependencies(Object[] dependencies) throws InvalidUserDataException;

    /**
     * Adds a dependency to the build script classpath. See{@link DependencyManager#dependency(java.util.List, Object)}
     * for more details.
     *
     * @param dependency The dependency to add.
     * @return The newly added dependency
     * @throws InvalidUserDataException When the given object cannot be converted to a {@code Dependency}.
     */
    Dependency dependency(Object dependency) throws InvalidUserDataException;

    /**
     * Adds a dependency to the build script classpath. See{@link DependencyManager#dependency(java.util.List, Object,
     * groovy.lang.Closure)} for more details.
     *
     * @param dependency The dependency to add.
     * @param configureClosure The closure to use to configure the dependency.
     * @throws InvalidUserDataException When one of the given object cannot be treated as a dependency.
     */
    Dependency dependency(Object dependency, Closure configureClosure) throws InvalidUserDataException;

    /**
     * <p>Returns the set of resolvers used to resolve the build script classpath.</p>
     *
     * @return the resolvers. Never returns null.
     */
    ResolverContainer getResolvers();

    FileSystemResolver addFlatDirResolver(String name, Object[] dirs);

    /**
     * <p>Returns the settings directory of the build. The settings directory is the directory containing the settings
     * file.</p>
     *
     * @return The settings directory. Never returns null.
     */
    File getSettingsDir();

    /**
     * <p>Returns the root directory of the build. The root directory is the project directory of the root project.</p>
     *
     * @return The root directory. Never returns null.
     */
    File getRootDir();

    /**
     * @param jarRepoUrls A list of urls of repositories to look for artifacts only. This is needed if only the pom is
     */
    DependencyResolver addMavenRepo(String[] jarRepoUrls);

    DependencyResolver addMavenStyleRepo(String name, String root, String[] jarRepoUrls);

    /**
     * <p>Returns the root project of the build.</p>
     *
     * @return The root project. Never returns null.
     */
    ProjectDescriptor getRootProject();

    /**
     * <p>Returns the project with the given path.</p>
     *
     * @param path The path.
     * @return The project with the given path. Never returns null.
     * @throws UnknownProjectException If no project with the given path exists.
     */
    ProjectDescriptor project(String path) throws UnknownProjectException;

    /**
     * <p>Returns the project with the given path.</p>
     *
     * @param path The path
     * @return The project with the given path. Returns null if no such project exists.
     */
    ProjectDescriptor findProject(String path);

    /**
     * <p>Returns the project with the given project directory.</p>
     *
     * @param projectDir The project directory.
     * @return The project with the given project directory. Never returns null.
     * @throws UnknownProjectException If no project with the given path exists.
     */
    ProjectDescriptor project(File projectDir) throws UnknownProjectException;

    /**
     * <p>Returns the project with the given project directory.</p>
     *
     * @param projectDir The project directory.
     * @return The project with the given project directory. Returns null if no such project exists.
     */
    ProjectDescriptor findProject(File projectDir);

    /**
     * <p>Returns the set of parameters used to invoke this instance of Gradle.</p>
     *
     * @return The parameters. Never returns null.
     */
    StartParameter getStartParameter();

    void clientModule(String id, Closure configureClosure);
}