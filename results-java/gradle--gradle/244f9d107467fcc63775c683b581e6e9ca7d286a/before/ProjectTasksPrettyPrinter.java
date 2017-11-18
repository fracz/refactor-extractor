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

package org.gradle.configuration;

import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Formatter;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Hans Dockter
 */
public class ProjectTasksPrettyPrinter {
    public static final String SEPARATOR = "**************************************************";

    public String getPrettyText(Map<Project, Set<Task>> tasks) {
        Formatter formatter = new Formatter();
        SortedSet<Project> sortedProjects = new TreeSet<Project>(tasks.keySet());
        for (Project project : sortedProjects) {
            formatter.format("%n%s%n", SEPARATOR);
            formatter.format("Project %s%n", project.getPath());
            SortedSet<Task> sortedTasks = new TreeSet<Task>(tasks.get(project));
            for (Task task : sortedTasks) {
                SortedSet<Task> sortedDependencies = new TreeSet<Task>(task.getTaskDependencies().getDependencies(task));
                formatter.format("  Task %s %s%n", task.getPath(), sortedDependencies);
            }
        }
        return formatter.toString();
    }
}