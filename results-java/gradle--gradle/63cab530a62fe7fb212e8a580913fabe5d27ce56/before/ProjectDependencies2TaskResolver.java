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
import org.gradle.api.ProjectAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hans Dockter
 */
public class ProjectDependencies2TaskResolver {
    private static Logger logger = LoggerFactory.getLogger(ProjectDependencies2TaskResolver.class);

    public void resolve(Project rootProject) {
        ProjectAction projectAction = new ProjectAction() {
            public void execute(Project project) {
                for (Project dependsOnProject : project.getDependsOnProjects()) {
                    logger.debug("Checking task dependencies for project: $project dependsOn: $dependsOnProject");
                    for (String taskName : project.getTasks().keySet()) {
                        if (dependsOnProject.getTasks().get(taskName) != null) {
                            logger.debug("Setting task dependencies for task: " + taskName);
                            project.getTasks().get(taskName).dependsOn(dependsOnProject.getTasks().get(taskName).getPath());
                        }
                    }
                }
            }
        };
        rootProject.allprojects(projectAction);
    }
}