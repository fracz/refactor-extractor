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

import groovy.lang.Closure;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.resolver.*;
import org.gradle.api.DependencyManager;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Transformer;
import org.gradle.api.dependencies.maven.Conf2ScopeMappingContainer;
import org.gradle.api.dependencies.maven.PomFilterContainer;
import org.gradle.api.dependencies.maven.GroovyPomFilterContainer;
import org.gradle.api.dependencies.maven.CopyableGroovyPomFilterContainer;
import org.gradle.api.internal.dependencies.maven.dependencies.DefaultConf2ScopeMappingContainer;
import org.gradle.api.internal.dependencies.maven.deploy.groovy.DefaultGroovyPomFilterContainer;
import org.gradle.api.internal.dependencies.maven.DefaultMavenPomFactory;
import org.gradle.api.dependencies.*;
import org.gradle.util.GUtil;
import org.gradle.util.ConfigureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * @author Hans Dockter
 */
public class BaseDependencyManager extends DefaultDependencyContainer
        implements DependencyManagerInternal, IvyObjectBuilder<DefaultModuleDescriptor> {
    private static Logger logger = LoggerFactory.getLogger(DefaultDependencyManager.class);

    private Map<String, DefaultConfiguration> configurations = new HashMap<String, DefaultConfiguration>();

    private Map<String, List<PublishArtifact>> artifacts = new HashMap<String, List<PublishArtifact>>();

    private Map<String, List<Artifact>> artifactDescriptors = new HashMap<String, List<Artifact>>();

    private List<String> absoluteArtifactPatterns = new ArrayList<String>();

    private Set<File> artifactParentDirs = new HashSet<File>();

    private String defaultArtifactPattern = DependencyManager.DEFAULT_ARTIFACT_PATTERN;

    private ResolverFactory resolverFactory;

    private IIvyFactory ivyFactory;

    private DefaultSettingsConverter settingsConverter;

    private ModuleDescriptorConverter moduleDescriptorConverter;

    private IDependencyResolver dependencyResolver;

    private IDependencyPublisher dependencyPublisher;

    private BuildResolverHandler buildResolverHandler;

    private ResolverContainer classpathResolvers;

    private Conf2ScopeMappingContainer defaultConf2ScopeMapping = new DefaultConf2ScopeMappingContainer();

    private String artifactProductionTaskName;

    private Map<String, Set<String>> confs4Task = new HashMap<String, Set<String>>();

    private Map<String, Set<String>> tasks4Conf = new HashMap<String, Set<String>>();

    private boolean failForMissingDependencies = true;

    private ExcludeRuleContainer excludeRules;

    public BaseDependencyManager() {

    }

    public BaseDependencyManager(IIvyFactory ivyFactory, DependencyFactory dependencyFactory,
                                 ResolverFactory resolverFactory, DefaultSettingsConverter settingsConverter, ModuleDescriptorConverter moduleDescriptorConverter,
                                 IDependencyResolver dependencyResolver, IDependencyPublisher dependencyPublisher,
                                 BuildResolverHandler buildResolverHandler, ExcludeRuleContainer excludeRuleContainer) {
        super(dependencyFactory, new ArrayList());
        this.ivyFactory = ivyFactory;
        this.resolverFactory = resolverFactory;
        this.settingsConverter = settingsConverter;
        this.moduleDescriptorConverter = moduleDescriptorConverter;
        this.dependencyResolver = dependencyResolver;
        this.dependencyPublisher = dependencyPublisher;
        this.buildResolverHandler = buildResolverHandler;
        this.excludeRules = excludeRuleContainer;
        this.classpathResolvers = new ResolverContainer(resolverFactory);
    }

    public List<File> resolve(String conf, boolean failForMissingDependencies, boolean includeProjectDependencies) {
        return dependencyResolver.resolve(conf, getIvy(), createModuleDescriptor(includeProjectDependencies),
                failForMissingDependencies);
    }

    public List<File> resolve(String conf) {
        return resolve(conf, this.failForMissingDependencies, true);
    }

    public List<File> resolveTask(String taskName) {
        Set<String> confs = confs4Task.get(taskName);
        if (!GUtil.isTrue(confs)) {
            throw new InvalidUserDataException(String.format("Task %s is not mapped to any conf!", taskName));
        }
        Set<File> paths = new LinkedHashSet<File>();
        for (String conf : confs) {
            paths.addAll(resolve(conf));
        }
        return new ArrayList<File>(paths);
    }

    public String antpath(String conf) {
        return GUtil.join(resolve(conf), ":");
    }

    public void publish(List<String> configurations, ResolverContainer resolvers, boolean uploadModuleDescriptor) {
        dependencyPublisher.publish(
                configurations,
                resolvers,
                createModuleDescriptor(true),
                uploadModuleDescriptor,
                getProject().getBuildDir(),
                this,
                ivy(resolvers.getResolverList()).getPublishEngine()
        );
    }

    public ModuleRevisionId createModuleRevisionId() {
        Object group = DependencyManager.DEFAULT_GROUP;
        Object version = DependencyManager.DEFAULT_VERSION;
        if (getProject().hasProperty("group") && GUtil.isTrue(getProject().property("group"))) {
            group = getProject().property("group");
        }
        if (getProject().hasProperty("version") && GUtil.isTrue(getProject().property("version"))) {
            version = getProject().property("version");
        }
        return new ModuleRevisionId(new ModuleId(group.toString(), getProject().getName()), version.toString());
    }

    public Ivy getIvy() {
        return ivy(new ArrayList<DependencyResolver>());
    }

    Ivy ivy(List<DependencyResolver> resolvers) {
        return ivyFactory.createIvy(
                settingsConverter.convert(
                        classpathResolvers.getResolverList(),
                        resolvers,
                        new File(getProject().getGradleUserHome()),
                        getBuildResolver(),
                        getClientModuleRegistry()
                )
        );
    }

    public DependencyManager linkConfWithTask(String conf, String task) {
        if (!GUtil.isTrue(conf) || !GUtil.isTrue(task)) {
            throw new InvalidUserDataException("Conf and tasks must be specified!");
        }
        if (tasks4Conf.get(conf) == null) {
            tasks4Conf.put(conf, new LinkedHashSet<String>());
        }
        if (confs4Task.get(task) == null) {
            confs4Task.put(task, new LinkedHashSet<String>());
        }
        tasks4Conf.get(conf).add(task);
        confs4Task.get(task).add(conf);
        return this;
    }

    public DependencyManager unlinkConfWithTask(String conf, String task) {
        if (!GUtil.isTrue(conf) || !GUtil.isTrue(task)) {
            throw new InvalidUserDataException("Conf and tasks must be specified!");
        }
        if (tasks4Conf.get(conf) == null || !tasks4Conf.get(conf).contains(task)) {
            throw new InvalidUserDataException("Can not unlink Conf= $conf and Task=$task because they are not linked!");
        }
        tasks4Conf.get(conf).remove(task);
        assert confs4Task.get(task) != null;
        confs4Task.get(task).remove(conf);
        return this;
    }

    public void addArtifacts(String configurationName, PublishArtifact... artifacts) {
        if (this.artifacts.get(configurationName) == null) {
            this.artifacts.put(configurationName, new ArrayList<PublishArtifact>());
        }
        for (PublishArtifact artifact : artifacts) {
            this.artifacts.get(configurationName).add(artifact);
        }
    }

    public Configuration addConfiguration(String configuration) {
        return addConfiguration(configuration, null);
    }

    public Configuration addConfiguration(String name, Closure configureClosure) {
        if (configurations.containsKey(name)) {
            throw new InvalidUserDataException(String.format("Cannot add configuration '%s' as a configuration with that name already exists.",
                    name));
        }
        DefaultConfiguration configuration = new DefaultConfiguration(name, this);
        configurations.put(name, configuration);
        ConfigureUtil.configure(configureClosure, configuration);
        return configuration;
    }

    public RepositoryResolver getBuildResolver() {
        return buildResolverHandler.getBuildResolver();
    }

    public File getBuildResolverDir() {
        return buildResolverHandler.getBuildResolverDir();
    }

    public FileSystemResolver addFlatDirResolver(String name, Object... dirs) {
        List<File> dirFiles = new ArrayList<File>();
        for (Object dir : dirs) {
            dirFiles.add(new File(dir.toString()));
        }
        FileSystemResolver resolver = classpathResolvers.createFlatDirResolver(name, dirFiles.toArray(new File[dirFiles.size()]));
        return (FileSystemResolver) classpathResolvers.add(resolver);
    }

    public DependencyResolver addMavenRepo(String... jarRepoUrls) {
        return classpathResolvers.add(classpathResolvers.createMavenRepoResolver(DependencyManager.DEFAULT_MAVEN_REPO_NAME,
                DependencyManager.MAVEN_REPO_URL, jarRepoUrls));
    }

    public DependencyResolver addMavenStyleRepo(String name, String root, String... jarRepoUrls) {
        return classpathResolvers.add(classpathResolvers.createMavenRepoResolver(name, root, jarRepoUrls));
    }

    public Map<String, Configuration> getConfigurations() {
        return new HashMap<String, Configuration>(configurations);
    }

    public void setConfigurations(Map<String, DefaultConfiguration> configurations) {
        this.configurations = configurations;
    }

    public boolean isFailForMissingDependencies() {
        return failForMissingDependencies;
    }

    public void setFailForMissingDependencies(boolean failForMissingDependencies) {
        this.failForMissingDependencies = failForMissingDependencies;
    }

    public Map<String, List<PublishArtifact>> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Map<String, List<PublishArtifact>> artifacts) {
        this.artifacts = artifacts;
    }

    public Map<String, List<Artifact>> getArtifactDescriptors() {
        return artifactDescriptors;
    }

    public void setArtifactDescriptors(Map<String, List<Artifact>> artifactDescriptors) {
        this.artifactDescriptors = artifactDescriptors;
    }

    public List<String> getAbsoluteArtifactPatterns() {
        return absoluteArtifactPatterns;
    }

    public void setAbsoluteArtifactPatterns(List<String> absoluteArtifactPatterns) {
        this.absoluteArtifactPatterns = absoluteArtifactPatterns;
    }

    public Set<File> getArtifactParentDirs() {
        return artifactParentDirs;
    }

    public void setArtifactParentDirs(Set<File> artifactParentDirs) {
        this.artifactParentDirs = artifactParentDirs;
    }

    public String getDefaultArtifactPattern() {
        return defaultArtifactPattern;
    }

    public void setDefaultArtifactPattern(String defaultArtifactPattern) {
        this.defaultArtifactPattern = defaultArtifactPattern;
    }

    public IIvyFactory getIvyFactory() {
        return ivyFactory;
    }

    public void setIvyFactory(IIvyFactory ivyFactory) {
        this.ivyFactory = ivyFactory;
    }

    public SettingsConverter getSettingsConverter() {
        return settingsConverter;
    }

    public void setSettingsConverter(DefaultSettingsConverter settingsConverter) {
        this.settingsConverter = settingsConverter;
    }

    public ModuleDescriptorConverter getModuleDescriptorConverter() {
        return moduleDescriptorConverter;
    }

    public void setModuleDescriptorConverter(ModuleDescriptorConverter moduleDescriptorConverter) {
        this.moduleDescriptorConverter = moduleDescriptorConverter;
    }

    public IDependencyResolver getDependencyResolver() {
        return dependencyResolver;
    }

    public void setDependencyResolver(IDependencyResolver dependencyResolver) {
        this.dependencyResolver = dependencyResolver;
    }

    public IDependencyPublisher getDependencyPublisher() {
        return dependencyPublisher;
    }

    public void setDependencyPublisher(IDependencyPublisher dependencyPublisher) {
        this.dependencyPublisher = dependencyPublisher;
    }

    public BuildResolverHandler getBuildResolverHandler() {
        return buildResolverHandler;
    }

    public void setBuildResolverHandler(BuildResolverHandler buildResolverHandler) {
        this.buildResolverHandler = buildResolverHandler;
    }

    public ResolverContainer getClasspathResolvers() {
        return classpathResolvers;
    }

    public void setClasspathResolvers(ResolverContainer classpathResolvers) {
        this.classpathResolvers = classpathResolvers;
    }

    public String getArtifactProductionTaskName() {
        return artifactProductionTaskName;
    }

    public void setArtifactProductionTaskName(String artifactProductionTaskName) {
        this.artifactProductionTaskName = artifactProductionTaskName;
    }

    public Map<String, Set<String>> getConfs4Task() {
        return confs4Task;
    }

    public void setConfs4Task(Map<String, Set<String>> confs4Task) {
        this.confs4Task = confs4Task;
    }

    public Map<String, Set<String>> getTasks4Conf() {
        return tasks4Conf;
    }

    public void setTasks4Conf(Map<String, Set<String>> tasks4Conf) {
        this.tasks4Conf = tasks4Conf;
    }

    public ExcludeRuleContainer getExcludeRules() {
        return excludeRules;
    }

    public ModuleDescriptor createModuleDescriptor(boolean includeProjectDependencies) {
        return moduleDescriptorConverter.convert(this, includeProjectDependencies);
    }

    public Conf2ScopeMappingContainer getDefaultMavenScopeMapping() {
        return defaultConf2ScopeMapping;
    }

    public void setDefaultConf2ScopeMapping(Conf2ScopeMappingContainer defaultConf2ScopeMapping) {
        this.defaultConf2ScopeMapping = defaultConf2ScopeMapping;
    }

    public void setExcludeRules(ExcludeRuleContainer excludeRules) {
        this.excludeRules = excludeRules;
    }

    public Configuration findConfiguration(String name) {
        return configurations.get(name);
    }

    public Configuration configuration(String name) throws UnknownConfigurationException {
        Configuration configuration = findConfiguration(name);
        if (configuration == null) {
            throw new UnknownConfigurationException(String.format("Configuration with name '%s' not found.", name));
        }
        return configuration;
    }

    public Configuration configuration(String name, Closure configureClosure) throws UnknownConfigurationException {
        Configuration configuration = configuration(name);
        ConfigureUtil.configure(configureClosure, configuration);
        return configuration;
    }

    public ResolverFactory getResolverFactory() {
        return resolverFactory;
    }

    public void addIvyTransformer(Transformer<DefaultModuleDescriptor> transformer) {
        moduleDescriptorConverter.addIvyTransformer(transformer);
    }

    public void addIvyTransformer(Closure transformer) {
        moduleDescriptorConverter.addIvyTransformer(transformer);
    }
}