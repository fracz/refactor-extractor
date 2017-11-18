/*
 * Copyright 2010-2014 the original author or authors.
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

package org.springframework.boot.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.bind.PropertySourcesPropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.env.PropertySourcesLoader;
import org.springframework.boot.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link ApplicationListener} that configures the context environment by loading
 * properties from well known file locations. By default properties will be loaded from
 * 'application.properties' and/or 'application.yml' files in the following locations:
 * <ul>
 * <li>classpath:</li>
 * <li>file:./</li>
 * <li>classpath:config/</li>
 * <li>file:./config/:</li>
 * </ul>
 * <p>
 * Alternative search locations and names can be specified using
 * {@link #setSearchLocations(String)} and {@link #setSearchNames(String)}.
 * <p>
 * Additional files will also be loaded based on active profiles. For example if a 'web'
 * profile is active 'application-web.properties' and 'application-web.yml' will be
 * considered.
 * <p>
 * The 'spring.config.name' property can be used to specify an alternative name to load
 * and the 'spring.config.location' property can be used to specify alternative search
 * locations or specific files.
 * <p>
 * Configuration properties are also bound to the {@link SpringApplication}. This makes it
 * possible to set {@link SpringApplication} properties dynamically, like the sources
 * ("spring.main.sources" - a CSV list) the flag to indicate a web environment
 * ("spring.main.web_environment=true") or the flag to switch off the banner
 * ("spring.main.show_banner=false").
 *
 * @author Dave Syer
 * @author Phillip Webb
 */
public class ConfigFileApplicationListener implements
		ApplicationListener<ApplicationEvent>, Ordered {

	private static final String DEFAULT_PROPERTIES = "defaultProperties";

	private static final String ACTIVE_PROFILES_PROPERTY = "spring.profiles.active";

	private static final String CONFIG_NAME_PROPERTY = "spring.config.name";

	private static final String CONFIG_LOCATION_PROPERTY = "spring.config.location";

	private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,file:./,"
			+ "classpath:/config/,file:./config/";

	private static final String DEFAULT_NAMES = "application";

	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

	private String searchLocations;

	private String names;

	private int order = DEFAULT_ORDER;

	private final ConversionService conversionService = new DefaultConversionService();

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationEnvironmentPreparedEvent) {
			onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
		}
		if (event instanceof ApplicationPreparedEvent) {
			onApplicationPreparedEvent((ApplicationPreparedEvent) event);
		}
	};

	private void onApplicationEnvironmentPreparedEvent(
			ApplicationEnvironmentPreparedEvent event) {
		Environment environment = event.getEnvironment();
		if (environment instanceof ConfigurableEnvironment) {
			onApplicationEnvironmentPreparedEvent((ConfigurableEnvironment) environment,
					event.getSpringApplication());
		}
	}

	private void onApplicationEnvironmentPreparedEvent(
			ConfigurableEnvironment environment, SpringApplication application) {
		RandomValuePropertySource.addToEnvironment(environment);
		try {
			PropertySource<?> defaultProperties = environment.getPropertySources()
					.remove(DEFAULT_PROPERTIES);
			new Loader(environment).load();
			if (defaultProperties != null) {
				environment.getPropertySources().addLast(defaultProperties);
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to load configuration files", ex);
		}
		bindToSpringApplication(application, environment);
	}

	private void bindToSpringApplication(SpringApplication application,
			ConfigurableEnvironment environment) {
		RelaxedDataBinder binder = new RelaxedDataBinder(application, "spring.main");
		binder.setConversionService(this.conversionService);
		binder.bind(new PropertySourcesPropertyValues(environment.getPropertySources()));
	}

	private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
		ConfigurableApplicationContext context = event.getApplicationContext();
		context.addBeanFactoryPostProcessor(new PropertySourceOrderingPostProcessor(
				context));
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * Set the search locations that will be considered as a comma-separated list.
	 */
	public void setSearchLocations(String locations) {
		Assert.hasLength(locations, "Locations must not be empty");
		this.searchLocations = locations;
	}

	/**
	 * Sets the names of the files that should be loaded (excluding file extension) as a
	 * comma-separated list.
	 */
	public void setSearchNames(String names) {
		Assert.hasLength(names, "Names must not be empty");
		this.names = names;
	}

	/**
	 * {@link BeanFactoryPostProcessor} to re-order our property sources below any
	 * {@code @ProperySource} items added by the {@link ConfigurationClassPostProcessor}.
	 */
	private class PropertySourceOrderingPostProcessor implements
			BeanFactoryPostProcessor, Ordered {

		private ConfigurableApplicationContext context;

		public PropertySourceOrderingPostProcessor(ConfigurableApplicationContext context) {
			this.context = context;
		}

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
				throws BeansException {
			reorderSources(this.context.getEnvironment());
		}

		private void reorderSources(ConfigurableEnvironment environment) {
			ConfigurationPropertySources.finishAndRelocate(environment
					.getPropertySources());
			PropertySource<?> defaultProperties = environment.getPropertySources()
					.remove(DEFAULT_PROPERTIES);
			if (defaultProperties != null) {
				environment.getPropertySources().addLast(defaultProperties);
			}
		}

	}

	/**
	 * Loads candidate property sources and configures the active profiles.
	 */
	private class Loader {

		private final ConfigurableEnvironment environment;

		private final ResourceLoader resourceLoader = new DefaultResourceLoader();

		private PropertySourcesLoader propertiesLoader;

		private Queue<String> profiles;

		private boolean activatedProfiles;

		public Loader(ConfigurableEnvironment environment) {
			this.environment = environment;
		}

		public void load() throws IOException {
			this.propertiesLoader = new PropertySourcesLoader();
			this.profiles = new LinkedList<String>();
			this.profiles.add(null);
			this.profiles.addAll(Arrays.asList(this.environment.getActiveProfiles()));
			this.activatedProfiles = false;
			addActiveProfiles(this.environment.getProperty(ACTIVE_PROFILES_PROPERTY));

			while (!this.profiles.isEmpty()) {
				String profile = this.profiles.poll();
				for (String location : getSearchLocations()) {
					for (String name : getSearchNames()) {
						load(location, name, profile);
					}
				}
			}

			addConfigurationProperties(this.propertiesLoader.getPropertySources());
		}

		private void load(String location, String name, String profile)
				throws IOException {

			// Try to load directly from the location
			PropertySource<?> locationPropertySource = load(location, profile);

			// If that fails, try a search
			if (locationPropertySource == null) {
				for (String ext : this.propertiesLoader.getAllFileExtensions()) {
					if (profile != null) {
						// Try the profile specific file (with a null profile section)
						load(location + name + "-" + profile + "." + ext, null);
					}
					// Try the profile (if any) specific section of the normal file
					load(location + name + "." + ext, profile);
				}
			}
		}

		private PropertySource<?> load(String resourceLocation, String profile)
				throws IOException {
			Resource resource = this.resourceLoader.getResource(resourceLocation);
			if (resource != null && resource.exists()) {
				String name = "applicationConfig: " + resource.getDescription();
				if (StringUtils.hasLength(profile)) {
					name += " " + profile;
				}
				PropertySource<?> propertySource = this.propertiesLoader.load(resource,
						name, profile);
				if (propertySource != null) {
					addActiveProfiles(propertySource
							.getProperty(ACTIVE_PROFILES_PROPERTY));
				}
				return propertySource;
			}
			return null;
		}

		private void addActiveProfiles(Object property) {
			String profiles = (property == null ? null : property.toString());
			boolean profilesNotActivatedWhenCalled = !this.activatedProfiles;
			for (String profile : asResolvedSet(profiles, null)) {
				boolean addition = profile.startsWith("+");
				profile = (addition ? profile.substring(1) : profile);
				if (profilesNotActivatedWhenCalled || addition) {
					this.profiles.add(profile);
					this.environment.addActiveProfile(profile);
					this.activatedProfiles = true;
				}
			}
		}

		public Set<String> getSearchLocations() {
			Set<String> locations = new LinkedHashSet<String>();
			locations.addAll(asResolvedSet(
					ConfigFileApplicationListener.this.searchLocations,
					DEFAULT_SEARCH_LOCATIONS));
			if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
				for (String path : asResolvedSet(
						this.environment.getProperty(CONFIG_LOCATION_PROPERTY), null)) {
					if (!path.contains("$")) {
						if (!path.contains(":")) {
							path = "file:" + path;
						}
						path = StringUtils.cleanPath(path);
					}
					locations.add(path);
				}
			}
			return locations;
		}

		public Set<String> getSearchNames() {
			if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
				return asResolvedSet(this.environment.getProperty(CONFIG_NAME_PROPERTY),
						null);
			}
			return asResolvedSet(ConfigFileApplicationListener.this.names, DEFAULT_NAMES);
		}

		private Set<String> asResolvedSet(String value, String fallback) {
			List<String> list = Arrays.asList(StringUtils
					.commaDelimitedListToStringArray(value != null ? this.environment
							.resolvePlaceholders(value) : fallback));
			Collections.reverse(list);
			return new LinkedHashSet<String>(list);
		}

		private void addConfigurationProperties(MutablePropertySources sources) {
			List<PropertySource<?>> reorderedSources = new ArrayList<PropertySource<?>>();
			for (PropertySource<?> item : sources) {
				reorderedSources.add(item);
			}
			Collections.reverse(reorderedSources);
			this.environment.getPropertySources().addLast(
					new ConfigurationPropertySources(reorderedSources));
		}

	}

	/**
	 * Holds the configuration {@link PropertySource}s as they are loaded can relocate
	 * them once configuration classes have been processed.
	 */
	static class ConfigurationPropertySources extends PropertySource<Object> {

		private static final String NAME = "applicationConfigurationProperties";

		private final Collection<PropertySource<?>> sources;

		public ConfigurationPropertySources(Collection<PropertySource<?>> sources) {
			super(NAME);
			this.sources = sources;
		}

		@Override
		public Object getProperty(String name) {
			for (PropertySource<?> propertySource : this.sources) {
				Object value = propertySource.getProperty(name);
				if (value != null) {
					return value;
				}
			}
			return null;
		}

		public static void finishAndRelocate(MutablePropertySources propertySources) {
			ConfigurationPropertySources removed = (ConfigurationPropertySources) propertySources
					.remove(ConfigurationPropertySources.NAME);
			if (removed != null) {
				for (PropertySource<?> propertySource : removed.sources) {
					propertySources.addLast(propertySource);
				}
			}
		}

	}

}