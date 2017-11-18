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

package org.gradle.tooling.internal.idea;

import org.gradle.api.GradleException;
import org.gradle.tooling.internal.protocol.InternalIdeaProject;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: Szczepan Faber, created at: 7/25/11
 */
public class DefaultIdeaProject implements InternalIdeaProject, IdeaProject, Serializable {

//    public static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private List<? extends IdeaModule> children = new LinkedList<IdeaModule>();
    private String languageLevel;
    private String jdkName;

    public String getLanguageLevel() {
        return languageLevel;
    }

    public DefaultIdeaProject setLanguageLevel(String languageLevel) {
        this.languageLevel = languageLevel;
        return this;
    }

    public String getJdkName() {
        return jdkName;
    }

    public DefaultIdeaProject setJdkName(String jdkName) {
        this.jdkName = jdkName;
        return this;
    }

    public String getId() {
        return id;
    }

    public DefaultIdeaProject setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DefaultIdeaProject setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DefaultIdeaProject setDescription(String description) {
        this.description = description;
        return this;
    }

    public HierarchicalElement getParent() {
        return null;
    }

    public File getProjectDirectory() {
        throw new GradleException("This method should not be used.");
    }

    public String getPath() {
        throw new GradleException("This method should not be used.");
    }

    public DefaultIdeaProject setChildren(List<? extends IdeaModule> children) {
        this.children = children;
        return this;
    }

    public DomainObjectSet<? extends IdeaModule> getChildren() {
        return new ImmutableDomainObjectSet<IdeaModule>(children);
    }

    @Override
    public String toString() {
        return "DefaultIdeaProject{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", children count=" + children.size()
                + ", languageLevel='" + languageLevel + '\''
                + ", jdkName='" + jdkName + '\''
                + '}';
    }
}