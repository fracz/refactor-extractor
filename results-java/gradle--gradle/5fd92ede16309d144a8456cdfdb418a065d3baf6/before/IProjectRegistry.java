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
package org.gradle.api.internal.project;

import org.gradle.api.Project;

import java.util.Set;
import java.io.File;

/**
 * @author Hans Dockter
 */
public interface IProjectRegistry {
    void addProject(Project project);

    Project getProject(String path);

    Project getProject(File projectDir);

    Set<Project> getAllProjects(String path);

    Set<Project> getSubProjects(String path);

    void reset();
}