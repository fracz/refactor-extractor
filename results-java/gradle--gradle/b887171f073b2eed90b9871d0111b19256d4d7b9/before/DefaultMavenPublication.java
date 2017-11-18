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

package org.gradle.api.publish.maven.internal;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.Module;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.internal.notations.api.NotationParser;
import org.gradle.api.publish.maven.InvalidMavenPublicationException;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenArtifactSet;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.internal.artifact.DefaultMavenArtifactSet;
import org.gradle.api.publish.maven.internal.artifact.MavenArtifactKey;
import org.gradle.api.publish.maven.internal.publisher.MavenNormalizedPublication;
import org.gradle.api.specs.Spec;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.util.CollectionUtils;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultMavenPublication implements MavenPublicationInternal {

    private final String name;
    private final MavenPomInternal pom;
    private final MavenProjectIdentity projectIdentity;
    private final DefaultMavenArtifactSet mavenArtifacts;
    private final Set<Dependency> runtimeDependencies = new LinkedHashSet<Dependency>();
    private FileCollection pomFile;
    private SoftwareComponentInternal component;

    public DefaultMavenPublication(
            String name, Module module, NotationParser<MavenArtifact> mavenArtifactParser, Instantiator instantiator
    ) {
        this.name = name;
        // TODO:DAZ Don't use Module in MavenPublication stuff : fix this as part of making Publish Coordinates configurable
        this.projectIdentity = new PublicationProjectIdentity(module);
        mavenArtifacts = instantiator.newInstance(DefaultMavenArtifactSet.class, name, mavenArtifactParser);
        pom = instantiator.newInstance(DefaultMavenPom.class, this);
    }

    public String getName() {
        return name;
    }

    public MavenPomInternal getPom() {
        return pom;
    }

    public void setPomFile(FileCollection pomFile) {
        this.pomFile = pomFile;
    }

    public void pom(Action<? super MavenPom> configure) {
        configure.execute(pom);
    }

    public void from(SoftwareComponent component) {
        if (this.component != null) {
            throw new InvalidUserDataException("A MavenPublication cannot include multiple components");
        }
        this.component = (SoftwareComponentInternal) component;

        for (PublishArtifact publishArtifact : this.component.getArtifacts()) {
            artifact(publishArtifact);
        }

        runtimeDependencies.addAll(this.component.getRuntimeDependencies());
    }

    public MavenArtifact artifact(Object source) {
        return mavenArtifacts.artifact(source);
    }

    public MavenArtifact artifact(Object source, Action<? super MavenArtifact> config) {
        return mavenArtifacts.artifact(source, config);
    }

    public MavenArtifactSet getArtifacts() {
        return mavenArtifacts;
    }

    public void setArtifacts(Iterable<?> sources) {
        mavenArtifacts.clear();
        for (Object source : sources) {
            artifact(source);
        }
    }

    public FileCollection getPublishableFiles() {
        return new UnionFileCollection(mavenArtifacts.getFiles(), pomFile);
    }

    public MavenProjectIdentity getMavenProjectIdentity() {
        return projectIdentity;
    }

    public Set<Dependency> getRuntimeDependencies() {
        return runtimeDependencies;
    }

    public MavenNormalizedPublication asNormalisedPublication() {
        // TODO:DAZ Change this so that the MavenNormalizedPublication just has a set of artifacts. Move determination of 'main' artifact into MavenPublisher
        MavenNormalizedPublication mavenNormalizedPublication = new MavenNormalizedPublication(name, getPomFile(), getMainArtifact(), getAdditionalArtifacts());
        // TODO:DAZ Move this into MavenPublisher
        mavenNormalizedPublication.validateArtifacts();
        return mavenNormalizedPublication;
    }

    private MavenArtifact getMainArtifact() {
        // TODO:DAZ Move this logic into MavenPublisher - moving logic out of domain layer into service layer.
        Set<MavenArtifact> candidateMainArtifacts = CollectionUtils.filter(mavenArtifacts, new Spec<MavenArtifact>() {
            public boolean isSatisfiedBy(MavenArtifact element) {
                return element.getClassifier() == null || element.getClassifier().length() == 0;
            }
        });
        if (candidateMainArtifacts.isEmpty()) {
            return null;
        }
        if (candidateMainArtifacts.size() > 1) {
            throw new InvalidMavenPublicationException(String.format("Cannot determine main artifact for maven publication '%s': multiple artifacts found with empty classifier.", name));
        }
        return candidateMainArtifacts.iterator().next();
    }

    private Set<MavenArtifact> getAdditionalArtifacts() {
        MavenArtifact mainArtifact = getMainArtifact();
        Set<MavenArtifactKey> keys = new HashSet<MavenArtifactKey>();
        Set<MavenArtifact> additionalArtifacts = new LinkedHashSet<MavenArtifact>();
        for (MavenArtifact artifact : mavenArtifacts) {
            if (artifact == mainArtifact) {
                continue;
            }
            // TODO:DAZ Move this validation into the MavenPublisher service
            MavenArtifactKey key = new MavenArtifactKey(artifact);
            if (keys.contains(key)) {
                throw new InvalidMavenPublicationException(String.format("Cannot publish maven publication '%s': multiple artifacts with the identical extension '%s' and classifier '%s'.",
                        name, artifact.getExtension(), artifact.getClassifier()));
            }
            keys.add(key);
            additionalArtifacts.add(artifact);
        }
        return additionalArtifacts;
    }

    private File getPomFile() {
        if (pomFile == null) {
            throw new IllegalStateException("pomFile not set for publication");
        }
        return pomFile.getSingleFile();
    }

    private class PublicationProjectIdentity implements MavenProjectIdentity {
        private final Module module;

        private PublicationProjectIdentity(Module module) {
            this.module = module;
        }

        public String getArtifactId() {
            return module.getName();
        }

        public String getGroupId() {
            return module.getGroup();
        }

        public String getVersion() {
            return module.getVersion();
        }

        public String getPackaging() {
            return getMainArtifact() == null ? "pom" : getMainArtifact().getExtension();
        }
    }
}