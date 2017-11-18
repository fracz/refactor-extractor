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

package org.gradle.api.internal.artifacts.ivyservice.ivyresolve;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.latest.ArtifactInfo;
import org.apache.ivy.plugins.latest.ComparatorLatestStrategy;
import org.apache.ivy.plugins.resolver.ResolverSettings;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserResolverChain implements DependencyToModuleResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserResolverChain.class);

    private final List<LocalAwareModuleVersionRepository> moduleVersionRepositories = new ArrayList<LocalAwareModuleVersionRepository>();
    private final List<String> moduleVersionRepositoryNames = new ArrayList<String>();
    private ResolverSettings settings;

    public void setSettings(ResolverSettings settings) {
        this.settings = settings;
    }

    public void add(LocalAwareModuleVersionRepository repository) {
        moduleVersionRepositories.add(repository);
        moduleVersionRepositoryNames.add(repository.getName());
    }

    public void resolve(DependencyDescriptor dependencyDescriptor, BuildableModuleVersionResolveResult result) {
        final ModuleRevisionId dependencyRevisionId = dependencyDescriptor.getDependencyRevisionId();
        LOGGER.debug("Attempting to resolve module '{}' using repositories {}", dependencyRevisionId, moduleVersionRepositoryNames);
        List<Throwable> errors = new ArrayList<Throwable>();
        final ModuleResolution latestResolved = findLatestModule(dependencyDescriptor, errors);
        if (latestResolved != null) {
            final ModuleVersionDescriptor downloadedModule = latestResolved.module;
            LOGGER.debug("Using module '{}' from repository '{}'", downloadedModule.getId(), latestResolved.repository.getName());
            for (Throwable error : errors) {
                LOGGER.debug("Discarding resolve failure.", error);
            }
            result.resolved(latestResolved.getId(), latestResolved.getDescriptor(), new ModuleVersionRepositoryArtifactResolverAdapter(latestResolved.repository, latestResolved.moduleSource));
            return;
        }
        if (!errors.isEmpty()) {
            result.failed(new ModuleVersionResolveException(dependencyRevisionId, errors));
        } else {
            final DefaultModuleVersionIdentifier moduleVersionIdentifier = new DefaultModuleVersionIdentifier(dependencyRevisionId.getOrganisation(), dependencyRevisionId.getName(), dependencyRevisionId.getRevision());
            result.notFound(moduleVersionIdentifier);
        }
    }

    private ModuleResolution findLatestModule(DependencyDescriptor dependencyDescriptor, Collection<Throwable> failures) {
        LinkedList<RepositoryResolveState> queue = new LinkedList<RepositoryResolveState>();
        for (LocalAwareModuleVersionRepository repository : moduleVersionRepositories) {
            queue.add(new RepositoryResolveState(repository));
        }
        LinkedList<RepositoryResolveState> missing = new LinkedList<RepositoryResolveState>();

        // A first pass to do local resolves only
        ModuleResolution best = findLatestModule(dependencyDescriptor, queue, failures, missing);
        if (best != null) {
            return best;
        }

        // Nothing found - do a second pass
        queue.addAll(missing);
        missing.clear();
        return findLatestModule(dependencyDescriptor, queue, failures, missing);
    }

    private ModuleResolution findLatestModule(DependencyDescriptor dependencyDescriptor, LinkedList<RepositoryResolveState> queue, Collection<Throwable> failures, Collection<RepositoryResolveState> missing) {
        boolean isStaticVersion = !settings.getVersionMatcher().isDynamic(dependencyDescriptor.getDependencyRevisionId());
        ModuleResolution best = null;
        while (!queue.isEmpty()) {
            RepositoryResolveState request = queue.removeFirst();
            try {
                request.resolve(dependencyDescriptor);
            } catch (Throwable t) {
                failures.add(t);
                continue;
            }
            switch (request.descriptor.getState()) {
                case Missing:
                    break;
                case ProbablyMissing:
                    // Queue this up for checking again later
                    if (request.canMakeFurtherAttempts()) {
                        missing.add(request);
                    }
                    break;
                case Unknown:
                    // Resolve again now
                    if (request.canMakeFurtherAttempts()) {
                        queue.addFirst(request);
                    }
                    break;
                case Resolved:
                    ModuleResolution moduleResolution = new ModuleResolution(request.repository, request.descriptor, request.descriptor.getModuleSource());
                    if (isStaticVersion && !moduleResolution.isGeneratedModuleDescriptor()) {
                        return moduleResolution;
                    }
                    best = chooseBest(best, moduleResolution);
                    break;
                default:
                    throw new IllegalStateException("Unexpected state for resolution: " + request.descriptor.getState());
            }
        }

        return best;
    }

    private ModuleResolution chooseBest(ModuleResolution one, ModuleResolution two) {
        if (one == null || two == null) {
            return two == null ? one : two;
        }
        if (one.module == null || two.module == null) {
            return two.module == null ? one : two;
        }

        ComparatorLatestStrategy latestStrategy = (ComparatorLatestStrategy) settings.getDefaultLatestStrategy();
        Comparator<ArtifactInfo> comparator = latestStrategy.getComparator();
        int comparison = comparator.compare(one, two);

        if (comparison == 0) {
            if (one.isGeneratedModuleDescriptor() && !two.isGeneratedModuleDescriptor()) {
                return two;
            }
            return one;
        }

        return comparison < 0 ? two : one;
    }

    private static class ModuleVersionRepositoryArtifactResolverAdapter implements ArtifactResolver {
        private final ModuleVersionRepository delegate;
        private final ModuleSource moduleSource;

        public ModuleVersionRepositoryArtifactResolverAdapter(ModuleVersionRepository repository, ModuleSource moduleSource) {
            this.delegate = repository;
            this.moduleSource = moduleSource;
        }

        public void resolve(Artifact artifact, BuildableArtifactResolveResult result) {
            delegate.resolve(artifact, result, moduleSource);
        }
    }

    private static class RepositoryResolveState {
        final LocalAwareModuleVersionRepository repository;
        final DefaultBuildableModuleVersionDescriptor descriptor = new DefaultBuildableModuleVersionDescriptor();

        boolean searchedLocally;
        boolean searchedRemotely;

        private RepositoryResolveState(LocalAwareModuleVersionRepository repository) {
            this.repository = repository;
        }

        void resolve(DependencyDescriptor dependencyDescriptor) {
            if (!searchedLocally) {
                searchedLocally = true;
                repository.getLocalDependency(dependencyDescriptor, descriptor);
            } else {
                searchedRemotely = true;
                repository.getDependency(dependencyDescriptor, descriptor);
            }
            if (descriptor.getState() == BuildableModuleVersionDescriptor.State.Failed) {
                throw descriptor.getFailure();
            }
        }

        public boolean canMakeFurtherAttempts() {
            return !searchedRemotely;
        }
    }

    private static class ModuleResolution implements ArtifactInfo {
        public final ModuleVersionRepository repository;
        public final ModuleVersionDescriptor module;
        public final ModuleSource moduleSource;

        public ModuleResolution(ModuleVersionRepository repository, ModuleVersionDescriptor module, ModuleSource moduleSource) {
            this.repository = repository;
            this.module = module;
            this.moduleSource = moduleSource;
        }

        public ModuleVersionIdentifier getId() throws ModuleVersionResolveException {
            return module.getId();
        }

        public ModuleDescriptor getDescriptor() throws ModuleVersionResolveException {
            return module.getDescriptor();
        }

        public boolean isGeneratedModuleDescriptor() {
            return module.getDescriptor().isDefault();
        }

        public long getLastModified() {
            return module.getDescriptor().getResolvedPublicationDate().getTime();
        }

        public String getRevision() {
            return module.getId().getVersion();
        }
    }
}