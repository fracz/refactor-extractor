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

import org.apache.ivy.plugins.resolver.*;
import org.gradle.api.DependencyManager;
import org.gradle.api.InvalidUserDataException;

import java.io.File;
import java.util.Map;

/**
 * @author Hans Dockter
 */
public class ResolverFactory {
    private LocalReposCacheHandler localReposCacheHandler;

    public ResolverFactory(LocalReposCacheHandler localReposCacheHandler) {
        this.localReposCacheHandler = localReposCacheHandler;
    }

    public DependencyResolver createResolver(Object userDescription) {
        DependencyResolver result;
        if (userDescription instanceof String) {
            result = createMavenRepoResolver((String) userDescription, (String) userDescription);
        } else if (userDescription instanceof Map) {
            Map<String, String> userDescriptionMap = (Map<String, String>) userDescription;
            result = createMavenRepoResolver(userDescriptionMap.get("name"), userDescriptionMap.get("url"));
        } else if (userDescription instanceof DependencyResolver) {
            result = (DependencyResolver) userDescription;
        } else {
            throw new InvalidUserDataException("Illegal Resolver type");
        }
        return result;
    }

    public FileSystemResolver createFlatDirResolver(String name, File... roots) {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName(name);
        resolver.setRepositoryCacheManager(localReposCacheHandler.getCacheManager());

        for (File root : roots) {
            String pattern = root.getAbsolutePath() + "/" + DependencyManager.FLAT_DIR_RESOLVER_PATTERN;
            resolver.addIvyPattern(pattern);
            resolver.addArtifactPattern(pattern);
        }
        resolver.setValidate(false);
        return resolver;
    }

    public DualResolver createMavenRepoResolver(String name, String root, String... jarRepoUrls) {
        IBiblioResolver iBiblioResolver = new IBiblioResolver();
        iBiblioResolver.setUsepoms(true);
        iBiblioResolver.setName(name + "_poms");
        iBiblioResolver.setRoot(root);
        iBiblioResolver.setPattern(DependencyManager.MAVEN_REPO_PATTERN);
        iBiblioResolver.setM2compatible(true);
        DualResolver dualResolver = new DualResolver();
        dualResolver.setName(name);
        dualResolver.setIvyResolver(iBiblioResolver);
        URLResolver urlResolver = new URLResolver();
        urlResolver.setName(name + "_jars");
        urlResolver.setM2compatible(true);
        urlResolver.addArtifactPattern(root + '/' + DependencyManager.MAVEN_REPO_PATTERN);
        for (String jarRepoUrl : jarRepoUrls) {
            urlResolver.addArtifactPattern(jarRepoUrl + '/' + DependencyManager.MAVEN_REPO_PATTERN);
        }
        dualResolver.setArtifactResolver(urlResolver);
        dualResolver.setAllownomd(false);
        return dualResolver;
    }
}