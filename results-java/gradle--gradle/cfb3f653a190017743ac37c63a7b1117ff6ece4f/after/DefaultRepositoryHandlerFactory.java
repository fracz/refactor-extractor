/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.artifacts.dsl;

import org.gradle.api.artifacts.dsl.RepositoryHandlerFactory;
import org.gradle.api.internal.artifacts.ivyservice.ResolverFactory;
import org.gradle.api.internal.ClassGenerator;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.plugins.Convention;

/**
 * @author Hans Dockter
 */
public class DefaultRepositoryHandlerFactory implements RepositoryHandlerFactory {
    private final ResolverFactory repositoryFactory;
    private final ClassGenerator classGenerator;

    public DefaultRepositoryHandlerFactory(ResolverFactory repositoryFactory, ClassGenerator classGenerator) {
        this.repositoryFactory = repositoryFactory;
        this.classGenerator = classGenerator;
    }

    public DefaultRepositoryHandler createRepositoryHandler(Convention convention) {
        DefaultRepositoryHandler repositoryHandler = classGenerator.newInstance(ConfigurableRepositoryHandler.class,
                repositoryFactory);
        ((IConventionAware) repositoryHandler).getConventionMapping().setConvention(convention);
        return repositoryHandler;
    }
}