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

package org.gradle.api.publish.ivy;

import org.gradle.api.Buildable;
import org.gradle.api.DefaultTask;
import org.gradle.api.Incubating;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.repositories.IvyArtifactRepositoryInternal;
import org.gradle.api.publish.ivy.internal.IvyNormalizedPublication;
import org.gradle.api.publish.ivy.internal.IvyPublicationInternal;
import org.gradle.api.publish.ivy.internal.IvyPublisher;
import org.gradle.api.tasks.TaskAction;

import java.util.concurrent.Callable;

/**
 * Publishes an IvyPublication to an IvyArtifactRepository.
 *
 * @since 1.3
 */
@Incubating
public class IvyPublish extends DefaultTask {

    private IvyPublicationInternal publication;
    private IvyArtifactRepositoryInternal repository;

    public IvyPublish() {
        // Allow the publication to participate in incremental build
        getInputs().files(new Callable<FileCollection>() {
            public FileCollection call() throws Exception {
                IvyPublicationInternal publicationInternal = getPublicationInternal();
                return publicationInternal == null ? null : publicationInternal.getPublishableFiles();
            }
        });

        // Allow the publication to have its dependencies fulfilled
        // There may be dependencies that aren't about creating files and not covered above
        dependsOn(new Callable<Buildable>() {
            public Buildable call() throws Exception {
                IvyPublicationInternal publicationInternal = getPublicationInternal();
                return publicationInternal == null ? null : publicationInternal;
            }
        });

        // Should repositories be able to participate in incremental?
        // At the least, they may be able to express themselves as output files
        // They *might* have input files and other dependencies as well though
        // Inputs: The credentials they need may be expressed in a file
        // Dependencies: Can't think of a case here
    }

    /**
     * The publication to be published.
     *
     * @return The publication to be published
     */
    public IvyPublication getPublication() {
        return publication;
    }

    /**
     * Sets the publication to be published.
     *
     * @param publication The publication to be published
     */
    public void setPublication(IvyPublication publication) {
        this.publication = toPublicationInternal(publication);
    }

    private IvyPublicationInternal getPublicationInternal() {
        return toPublicationInternal(getPublication());
    }

    private static IvyPublicationInternal toPublicationInternal(IvyPublication publication) {
        if (publication == null) {
            return null;
        } else if (publication instanceof IvyPublicationInternal) {
            return (IvyPublicationInternal) publication;
        } else {
            throw new InvalidUserDataException(
                    String.format(
                            "publication objects must implement the '%s' interface, implementation '%s' does not",
                            IvyPublicationInternal.class.getName(),
                            publication.getClass().getName()
                    )
            );
        }
    }

    /**
     * The repository to publish to.
     *
     * @return The repository to publish to
     */
    public IvyArtifactRepository getRepository() {
        return repository;
    }

    /**
     * Sets the repository to publish to.
     *
     * @param repository The repository to publish to
     */
    public void setRepository(IvyArtifactRepository repository) {
        this.repository = toRepositoryInternal(repository);
    }

    private IvyArtifactRepositoryInternal getRepositoryInternal() {
        return toRepositoryInternal(getRepository());
    }

    private static IvyArtifactRepositoryInternal toRepositoryInternal(IvyArtifactRepository repository) {
        if (repository == null) {
            return null;
        } else if (repository instanceof IvyArtifactRepositoryInternal) {
            return (IvyArtifactRepositoryInternal) repository;
        } else {
            throw new InvalidUserDataException(
                    String.format(
                            "repository objects must implement the '%s' interface, implementation '%s' does not",
                            IvyArtifactRepositoryInternal.class.getName(),
                            repository.getClass().getName()
                    )
            );
        }
    }

    @TaskAction
    public void publish() {
        IvyPublicationInternal publicationInternal = getPublicationInternal();
        if (publicationInternal == null) {
            throw new InvalidUserDataException("The 'publication' property is required");
        }

        IvyArtifactRepositoryInternal repositoryInternal = getRepositoryInternal();
        if (repositoryInternal == null) {
            throw new InvalidUserDataException("The 'repository' property is required");
        }

        doPublish(publicationInternal, repositoryInternal);
    }

    private void doPublish(IvyPublicationInternal publication, IvyArtifactRepositoryInternal repository) {
        IvyPublisher publisher = repository.createPublisher();
        IvyNormalizedPublication normalizedPublication = publication.asNormalisedPublication();
        publisher.publish(normalizedPublication);
    }

}