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

package org.gradle.api.internal.tasks.compile.incremental.jar;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.util.Clock;

import java.io.File;

public class JarSnapshotsMaker {

    private static final Logger LOG = Logging.getLogger(JarSnapshotsMaker.class);

    private final LocalJarClasspathSnapshot localJarClasspathSnapshot;
    private final ClasspathJarFinder classpathJarFinder;
    private final JarClasspathSnapshotFactory classpathSnapshotFactory;

    private JarClasspathSnapshot jarClasspathSnapshot;

    public JarSnapshotsMaker(LocalJarClasspathSnapshot localJarClasspathSnapshot, JarClasspathSnapshotFactory classpathSnapshotFactory, ClasspathJarFinder classpathJarFinder) {
        this.localJarClasspathSnapshot = localJarClasspathSnapshot;
        this.classpathSnapshotFactory = classpathSnapshotFactory;
        this.classpathJarFinder = classpathJarFinder;
    }

    public void storeJarSnapshots(Iterable<File> classpath) {
        maybeInitialize(classpath); //clients may or may not have already created jar classpath snapshot
        Clock clock = new Clock();
        localJarClasspathSnapshot.putClasspathSnapshot(jarClasspathSnapshot.getData());
        LOG.info("Written jar snapshots for incremental compilation in {}.", clock.getTime());
    }

    public JarClasspathSnapshot getJarClasspathSnapshot(Iterable<File> classpath) {
        maybeInitialize(classpath); //clients may or may not have already created jar classpath snapshot
        return jarClasspathSnapshot;
    }

    private void maybeInitialize(Iterable<File> classpath) {
        if (jarClasspathSnapshot != null) {
            return;
        }
        Clock clock = new Clock();
        Iterable<JarArchive> jarArchives = classpathJarFinder.findJarArchives(classpath);

        jarClasspathSnapshot = classpathSnapshotFactory.createSnapshot(jarArchives);
        LOG.info("Created jar snapshots for incremental compilation in {}.", clock.getTime());
    }
}