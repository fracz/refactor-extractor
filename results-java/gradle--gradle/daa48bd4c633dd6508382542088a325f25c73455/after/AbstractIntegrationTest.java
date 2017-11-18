/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.integtests.fixtures;

import org.gradle.integtests.fixtures.executer.*;
import org.gradle.test.fixtures.ivy.IvyFileRepository;
import org.gradle.test.fixtures.maven.MavenFileRepository;
import org.gradle.util.TestFile;
import org.gradle.util.TestFileContext;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;

public abstract class AbstractIntegrationTest implements TestFileContext {
    @Rule public final GradleDistribution distribution = new GradleDistribution();
    public final GradleDistributionExecuter executer = new GradleDistributionExecuter(distribution);

    @ClassRule public static final GradleDistribution SHARED_DISTRIBUTION = new GradleDistribution();
    private static final GradleDistributionExecuter SHARED_EXECUTER = new GradleDistributionExecuter(SHARED_DISTRIBUTION);

    @ClassRule public static final PerClassState perClassState = new PerClassState();

    protected boolean useSharedBuild;

    private MavenFileRepository mavenRepo;
    private IvyFileRepository ivyRepo;

    protected GradleDistribution getDistribution() {
        return useSharedBuild ? SHARED_DISTRIBUTION : distribution;
    }

    protected GradleDistributionExecuter getExecuter() {
        return useSharedBuild ? SHARED_EXECUTER : executer;
    }

    protected void runSharedBuild() {}

    @Before
    public void doRunSharedBuild() {
        if (!perClassState.sharedBuildRun) {
            useSharedBuild = true;
            runSharedBuild();
            useSharedBuild = false;
            perClassState.sharedBuildRun = true;
        }
    }

    public TestFile getTestDir() {
        return getDistribution().getTestDir();
    }

    public TestFile file(Object... path) {
        return getTestDir().file(path);
    }

    public TestFile testFile(String name) {
        return file(name);
    }

    protected GradleExecuter inTestDirectory() {
        return inDirectory(getTestDir());
    }

    protected GradleExecuter inDirectory(File directory) {
        return getExecuter().inDirectory(directory);
    }

    protected GradleExecuter usingBuildFile(File file) {
        return getExecuter().usingBuildScript(file);
    }

    protected GradleExecuter usingProjectDir(File projectDir) {
        return getExecuter().usingProjectDirectory(projectDir);
    }

    protected ArtifactBuilder artifactBuilder() {
        GradleDistributionExecuter gradleExecuter = getDistribution().executer();
        gradleExecuter.withGradleUserHomeDir(getDistribution().getUserHomeDir());
        return new GradleBackedArtifactBuilder(gradleExecuter, getTestDir().file("artifacts"));
    }

    public MavenFileRepository maven(TestFile repo) {
        return new MavenFileRepository(repo);
    }

    public MavenFileRepository maven(Object repo) {
        return new MavenFileRepository(file(repo));
    }

    public MavenFileRepository getMavenRepo() {
        if (mavenRepo == null) {
            mavenRepo = new MavenFileRepository(file("maven-repo"));
        }
        return mavenRepo;
    }

    public IvyFileRepository ivy(TestFile repo) {
        return new IvyFileRepository(repo);
    }

    public IvyFileRepository ivy(Object repo) {
        return new IvyFileRepository(file(repo));
    }

    public IvyFileRepository getIvyRepo() {
        if (ivyRepo == null) {
            ivyRepo = new IvyFileRepository(file("ivy-repo"));
        }
        return ivyRepo;
    }

    private static class PerClassState implements TestRule {
        boolean sharedBuildRun;

        public Statement apply(Statement base, Description description) {
            return base;
        }
    }
}