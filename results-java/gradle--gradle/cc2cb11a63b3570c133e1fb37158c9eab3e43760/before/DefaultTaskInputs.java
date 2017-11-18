/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.internal.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import groovy.lang.Closure;
import groovy.lang.GString;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.TaskInputsInternal;
import org.gradle.api.internal.file.CompositeFileCollection;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.file.collections.FileCollectionResolveContext;
import org.gradle.api.tasks.TaskInputFilePropertySpec;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.util.ConfigureUtil;
import org.gradle.util.DeprecationLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

import static org.gradle.util.GUtil.uncheckedCall;

public class DefaultTaskInputs implements TaskInputsInternal {
    private final FileCollection allInputFiles;
    private final FileCollection allSourceFiles;
    private final FileResolver resolver;
    private final String taskName;
    private final TaskMutator taskMutator;
    private final Map<String, Object> properties = new HashMap<String, Object>();
    private final List<PropertySpec> fileProperties = Lists.newArrayList();
    private int legacyFilePropertyCounter;
    private Queue<Action<? super TaskInputs>> configureActions;

    public DefaultTaskInputs(FileResolver resolver, String taskName, TaskMutator taskMutator) {
        this.resolver = resolver;
        this.taskName = taskName;
        this.taskMutator = taskMutator;
        this.allInputFiles = new TaskInputUnionFileCollection("task '" + taskName + "' input files", false);
        this.allSourceFiles = new TaskInputUnionFileCollection("task '" + taskName + "' source files", true);
    }

    @Override
    public boolean getHasInputs() {
        return !fileProperties.isEmpty() || !properties.isEmpty();
    }

    @Override
    public FileCollection getFiles() {
        return allInputFiles;
    }

    @Override
    public Collection<TaskInputFilePropertySpecInternal> getFileProperties() {
        ImmutableList.Builder<TaskInputFilePropertySpecInternal> builder = ImmutableList.builder();
        for (PropertySpec propertySpec : fileProperties) {
            if (propertySpec.getPropertyName() == null) {
                propertySpec.withPropertyName(nextLegacyPropertyName());
            }
            builder.add(propertySpec);
        }
        return builder.build();
    }

    @Override
    public TaskInputFilePropertySpec includeFile(final Object path) {
        return taskMutator.mutate("TaskInputs.includeFile(Object)", new Callable<TaskInputFilePropertySpec>() {
            @Override
            public TaskInputFilePropertySpec call() throws Exception {
                return addSpec(path);
            }
        });
    }

    @Override
    public TaskInputFilePropertySpec includeDir(final Object path) {
        return taskMutator.mutate("TaskInputs.includeDir(Object)", new Callable<TaskInputFilePropertySpec>() {
            @Override
            public TaskInputFilePropertySpec call() {
                return addSpec(resolver.resolveFilesAsTree(path));
            }
        });
    }

    @Override
    public TaskInputFilePropertySpec includeFiles(final Object... paths) {
        return taskMutator.mutate("TaskInputs.includeFiles(Object...)", new Callable<TaskInputFilePropertySpec>() {
            @Override
            public TaskInputFilePropertySpec call() {
                return addSpec(paths);
            }
        });
    }

    @Override
    public TaskInputs files(final Object... paths) {
        DeprecationLogger.nagUserOfReplacedMethod("TaskInputs.files(Object...)", "TaskInputs.includeFiles(Object...)");
        taskMutator.mutate("TaskInputs.files(Object...)", new Runnable() {
            @Override
            public void run() {
                addSpec(paths);
            }
        });
        return this;
    }

    @Override
    public TaskInputs file(final Object path) {
        DeprecationLogger.nagUserOfReplacedMethod("TaskInputs.file(Object)", "TaskInputs.includeFile(Object)");
        taskMutator.mutate("TaskInputs.file(Object)", new Runnable() {
            @Override
            public void run() {
                addSpec(path);
            }
        });
        return this;
    }

    @Override
    public TaskInputs dir(final Object dirPath) {
        DeprecationLogger.nagUserOfReplacedMethod("TaskInputs.dir(Object)", "TaskInputs.includeDir(Object)");
        taskMutator.mutate("TaskInputs.dir(Object)", new Runnable() {
            @Override
            public void run() {
                addSpec(resolver.resolveFilesAsTree(dirPath));
            }
        });
        return this;
    }

    @Override
    public boolean getHasSourceFiles() {
        for (PropertySpec propertySpec : fileProperties) {
            if (propertySpec.isSkipWhenEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FileCollection getSourceFiles() {
        return allSourceFiles;
    }

    @Override
    public TaskInputs source(final Object... paths) {
        DeprecationLogger.nagUserOfReplacedMethod("TaskInputs.source(Object...)", "TaskInputs.includeFiles(Object...)");
        taskMutator.mutate("TaskInputs.source(Object...)", new Runnable() {
            @Override
            public void run() {
                addSpec(paths, true);
            }
        });
        return this;
    }

    @Override
    public TaskInputs source(final Object path) {
        DeprecationLogger.nagUserOfReplacedMethod("TaskInputs.source(Object)", "TaskInputs.includeFile(Object)");
        taskMutator.mutate("TaskInputs.source(Object)", new Runnable() {
            @Override
            public void run() {
                addSpec(path, true);
            }
        });
        return this;
    }

    @Override
    public TaskInputs sourceDir(final Object path) {
        DeprecationLogger.nagUserOfReplacedMethod("TaskInputs.sourceDir(Object)", "TaskInputs.includeDir(Object)");
        taskMutator.mutate("TaskInputs.sourceDir(Object)", new Runnable() {
            @Override
            public void run() {
                addSpec(resolver.resolveFilesAsTree(path), true);
            }
        });
        return this;
    }

    private TaskInputFilePropertySpecInternal addSpec(Object paths) {
        return addSpec(paths, false);
    }

    private TaskInputFilePropertySpecInternal addSpec(Object paths, boolean skipWhenEmpty) {
        PropertySpec spec = new PropertySpec(taskName, skipWhenEmpty, resolver, paths);
        fileProperties.add(spec);
        return spec;
    }

    private String nextLegacyPropertyName() {
        return "$" + (++legacyFilePropertyCounter);
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> actualProperties = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Object value = prepareValue(entry.getValue());
            actualProperties.put(entry.getKey(), value);
        }
        return actualProperties;
    }

    private Object prepareValue(Object value) {
        while (true) {
            if (value instanceof Callable) {
                Callable callable = (Callable) value;
                value = uncheckedCall(callable);
            } else if (value instanceof FileCollection) {
                FileCollection fileCollection = (FileCollection) value;
                return fileCollection.getFiles();
            } else {
                return avoidGString(value);
            }
        }
    }

    private static Object avoidGString(Object value) {
        return (value instanceof GString) ? value.toString() : value;
    }

    public TaskInputs property(final String name, final Object value) {
        taskMutator.mutate("TaskInputs.property(String, Object)", new Runnable() {
            public void run() {
                properties.put(name, value);
            }
        });
        return this;
    }

    public TaskInputs properties(final Map<String, ?> newProps) {
        taskMutator.mutate("TaskInputs.properties(Map)", new Runnable() {
            public void run() {
                properties.putAll(newProps);
            }
        });
        return this;
    }

    @Override
    public TaskInputs configure(final Action<? super TaskInputs> action) {
        taskMutator.mutate("TaskInputs.configure(Action)", new Runnable() {
            public void run() {
                if (configureActions == null) {
                    configureActions = Lists.newLinkedList();
                }
                configureActions.add(action);
            }
        });
        return this;
    }

    @Override
    public TaskInputs configure(Closure action) {
        return configure(ConfigureUtil.configureUsing(action));
    }

    @Override
    public void ensureConfigured() {
        if (configureActions != null) {
            while (!configureActions.isEmpty()) {
                configureActions.remove().execute(this);
            }
            configureActions = null;
        }
    }

    private static class PropertySpec implements TaskInputFilePropertySpecInternal {

        private final TaskPropertyFileCollection files;
        private String propertyName;
        private boolean skipWhenEmpty;
        private boolean optional;

        public PropertySpec(String taskName, boolean skipWhenEmpty, FileResolver resolver, Object paths) {
            this.files = new TaskPropertyFileCollection(taskName, "input", this, resolver, paths);
            this.skipWhenEmpty = skipWhenEmpty;
        }

        @Override
        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public TaskInputFilePropertySpec withPropertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        @Override
        public TaskPropertyFileCollection getPropertyFiles() {
            return files;
        }

        @Override
        public boolean isSkipWhenEmpty() {
            return skipWhenEmpty;
        }

        @Override
        public TaskInputFilePropertySpecInternal skipWhenEmpty(boolean skipWhenEmpty) {
            this.skipWhenEmpty = skipWhenEmpty;
            return this;
        }

        @Override
        public TaskInputFilePropertySpec skipWhenEmpty() {
            return skipWhenEmpty(true);
        }

        @Override
        public boolean isOptional() {
            return optional;
        }

        @Override
        public TaskInputFilePropertySpecInternal optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        @Override
        public TaskInputFilePropertySpec optional() {
            return optional(true);
        }

        @Override
        public String toString() {
            return propertyName == null ? "<unnamed>" : propertyName;
        }
    }

    private class TaskInputUnionFileCollection extends CompositeFileCollection {
        private final boolean skipWhenEmptyOnly;
        private final String displayName;

        public TaskInputUnionFileCollection(String displayName, boolean skipWhenEmptyOnly) {
            this.displayName = displayName;
            this.skipWhenEmptyOnly = skipWhenEmptyOnly;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public void visitContents(FileCollectionResolveContext context) {
            for (PropertySpec fileProperty : fileProperties) {
                if (!skipWhenEmptyOnly || fileProperty.isSkipWhenEmpty()) {
                    context.add(fileProperty.getPropertyFiles());
                }
            }
        }
    }
}