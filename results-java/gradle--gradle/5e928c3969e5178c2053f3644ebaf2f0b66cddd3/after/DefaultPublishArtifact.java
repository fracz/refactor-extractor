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

package org.gradle.api.internal.dependencies;

import org.gradle.api.dependencies.Configuration;

import java.io.File;
import java.util.Set;

/**
 * @author Hans Dockter
 */
public class DefaultPublishArtifact extends AbstractPublishArtifact {
    private String name;
    private String extension;
    private String type;
    private String classifier;
    private File file;

    public DefaultPublishArtifact(Set<Configuration> configurations, String name, String extension, String type,
                                  String classifier, File file, Object... tasks) {
        super(configurations, tasks);
        this.name = name;
        this.extension = extension;
        this.type = type;
        this.classifier = classifier;
        this.file = file;
    }

    public String toString() {
        return String.format("DefaultPublishArtifact $s:%s:%s:%s", name, type, extension, classifier);
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public String getType() {
        return type;
    }

    public String getClassifier() {
        return classifier;
    }

    public File getFile() {
        return file;
    }
}