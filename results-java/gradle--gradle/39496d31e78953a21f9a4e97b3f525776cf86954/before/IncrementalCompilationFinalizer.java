/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.internal.tasks.compile.incremental;

import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.internal.tasks.compile.incremental.jar.ClasspathJarFinder;
import org.gradle.api.internal.tasks.compile.incremental.jar.JarSnapshotsMaker;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.gradle.util.Clock;

class IncrementalCompilationFinalizer implements Compiler<JavaCompileSpec> {

    private static final Logger LOG = Logging.getLogger(IncrementalCompilationFinalizer.class);

    private final Compiler<JavaCompileSpec> delegate;
    private final JarSnapshotsMaker jarSnapshotsMaker;
    private final ClasspathJarFinder classpathJarFinder;
    private final ClassDependencyInfoUpdater updater;

    public IncrementalCompilationFinalizer(Compiler<JavaCompileSpec> delegate, JarSnapshotsMaker jarSnapshotsMaker,
                                           ClasspathJarFinder classpathJarFinder, ClassDependencyInfoUpdater updater) {
        this.delegate = delegate;
        this.jarSnapshotsMaker = jarSnapshotsMaker;
        this.classpathJarFinder = classpathJarFinder;
        this.updater = updater;
    }

    public WorkResult execute(JavaCompileSpec spec) {
        WorkResult out = delegate.execute(spec);

        updater.updateInfo(spec, out);

        Clock clock = new Clock();
        jarSnapshotsMaker.storeJarSnapshots(classpathJarFinder.findJarArchives(spec.getClasspath()));
        LOG.lifecycle("Created and written jar snapshots in {}.", clock.getTime());

        return out;
    }
}