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
package org.gradle.language.base.plugins;

import org.gradle.api.*;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.Factory;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.language.base.FunctionalSourceSet;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.ProjectSourceSet;
import org.gradle.language.base.internal.DefaultLanguageRegistry;
import org.gradle.language.base.internal.DefaultProjectSourceSet;
import org.gradle.language.base.internal.LanguageRegistration;
import org.gradle.language.base.internal.LanguageRegistry;
import org.gradle.model.ModelRules;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.runtime.base.BinaryContainer;
import org.gradle.runtime.base.ProjectBinary;
import org.gradle.runtime.base.ProjectComponent;
import org.gradle.runtime.base.ProjectComponentContainer;
import org.gradle.runtime.base.internal.DefaultBinaryContainer;
import org.gradle.runtime.base.internal.DefaultProjectComponentContainer;
import org.gradle.runtime.base.internal.ProjectBinaryInternal;

import javax.inject.Inject;

/**
 * Base plugin for language support.
 *
 * Adds a {@link org.gradle.runtime.base.ProjectComponentContainer} named {@code projectComponents} to the project.
 * Adds a {@link org.gradle.runtime.base.BinaryContainer} named {@code binaries} to the project.
 * Adds a {@link org.gradle.language.base.ProjectSourceSet} named {@code sources} to the project.
 *
 * For each binary instance added to the binaries container, registers a lifecycle task to create that binary.
 */
@Incubating
public class LanguageBasePlugin implements Plugin<Project> {

    private final Instantiator instantiator;
    private final ModelRules modelRules;

    @Inject
    public LanguageBasePlugin(Instantiator instantiator, ModelRules modelRules) {
        this.instantiator = instantiator;
        this.modelRules = modelRules;
    }

    public void apply(final Project target) {
        target.getPlugins().apply(LifecycleBasePlugin.class);

        LanguageRegistry domainRegistry = target.getExtensions().create("languages", DefaultLanguageRegistry.class);

        // TODO:DAZ Rename to 'components' and merge with Project.components
        ProjectComponentContainer components = target.getExtensions().create("projectComponents", DefaultProjectComponentContainer.class, instantiator);
        ProjectSourceSet sources = target.getExtensions().create("sources", DefaultProjectSourceSet.class, instantiator);
        final BinaryContainer binaries = target.getExtensions().create("binaries", DefaultBinaryContainer.class, instantiator);

        modelRules.register("binaries", BinaryContainer.class, new Factory<BinaryContainer>() {
            public BinaryContainer create() {
                return binaries;
            }
        });

        binaries.withType(ProjectBinaryInternal.class).all(new Action<ProjectBinaryInternal>() {
            public void execute(ProjectBinaryInternal binary) {
                Task binaryLifecycleTask = target.task(binary.getNamingScheme().getLifecycleTaskName());
                binaryLifecycleTask.setGroup(LifecycleBasePlugin.BUILD_GROUP);
                binaryLifecycleTask.setDescription(String.format("Assembles %s.", binary));
                binary.setBuildTask(binaryLifecycleTask);
            }
        });
        createProjectSourceSetForEachComponent(sources, components);
        createLanguageSourceSets(target, domainRegistry, sources);
    }

    private void createLanguageSourceSets(final Project project, final LanguageRegistry domainRegistry, final ProjectSourceSet sources) {
        DomainObjectSet<LanguageRegistration> languages = domainRegistry.getLanguages();
        languages.all(new Action<LanguageRegistration>() {
            public void execute(final LanguageRegistration languageRegistration) {
                sources.all(new Action<FunctionalSourceSet>() {
                    public void execute(final FunctionalSourceSet functionalSourceSet) {
                        NamedDomainObjectFactory<? extends LanguageSourceSet> namedDomainObjectFactory = new NamedDomainObjectFactory<LanguageSourceSet>() {
                            public LanguageSourceSet create(String name) {
                                Class<? extends LanguageSourceSet> sourceSetImplementation = languageRegistration.getSourceSetImplementation();
                                return instantiator.newInstance(sourceSetImplementation, name, functionalSourceSet, project);
                            }
                        };
                        Class<? extends LanguageSourceSet> sourceSetType = languageRegistration.getSourceSetType();
                        functionalSourceSet.registerFactory((Class<LanguageSourceSet>) sourceSetType, namedDomainObjectFactory);

                        // Create a default language source set
                        functionalSourceSet.maybeCreate(languageRegistration.getName(), sourceSetType);
                    }
                });
            }
        });
    }

    private void createProjectSourceSetForEachComponent(final ProjectSourceSet sources, ProjectComponentContainer components) {
        // Create a functionalSourceSet for each native component, with the same name
        components.withType(ProjectComponent.class).all(new Action<ProjectComponent>() {
            public void execute(ProjectComponent component) {
                component.source(sources.maybeCreate(component.getName()));
            }
        });
    }

    /**
     * Model rules.
     */
    @RuleSource
    static class Rules {
        @Mutate
        @SuppressWarnings("UnusedDeclaration")
        void attachBinariesToLifecycle(TaskContainer tasks, BinaryContainer binaries) {
            Task assembleTask = tasks.getByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
            for (ProjectBinary binary : binaries.withType(ProjectBinary.class)) {
                if (binary.isBuildable()) {
                    assembleTask.dependsOn(binary);
                }
            }
        }
    }
}