/*
 * Copyright 2012-2013 the original author or authors.
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

package org.springframework.bootstrap.actuate.autoconfigure;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.bootstrap.actuate.endpoint.error.ErrorEndpoint;
import org.springframework.bootstrap.actuate.properties.ManagementServerProperties;
import org.springframework.bootstrap.context.annotation.ConditionalOnBean;
import org.springframework.bootstrap.context.embedded.ConfigurableEmbeddedServletContainerFactory;
import org.springframework.bootstrap.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.bootstrap.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.bootstrap.context.embedded.ErrorPage;
import org.springframework.bootstrap.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.bootstrap.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Configuration for creating a new container (e.g. tomcat) for the management endpoints.
 *
 * @author Dave Syer
 */
@Configuration
@EnableWebMvc
public class ManagementServerConfiguration {

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
			ApplicationContext context) {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public ErrorEndpoint errorEndpoint() {
		return new ErrorEndpoint();
	}

	@Bean
	@ConditionalOnBean(TomcatEmbeddedServletContainerFactory.class)
	public EmbeddedServletContainerFactory tomcatContainer(
			HierarchicalBeanFactory beanFactory) {
		TomcatEmbeddedServletContainerFactory factory = beanFactory
				.getParentBeanFactory().getBean(
						TomcatEmbeddedServletContainerFactory.class);
		return factory.getChildContextFactory("Management");
	}

	@Bean
	@ConditionalOnBean(JettyEmbeddedServletContainerFactory.class)
	public EmbeddedServletContainerFactory jettyContainer() {
		return new JettyEmbeddedServletContainerFactory();
	}

	@Component
	protected static class ServerCustomizationConfiguration implements
			EmbeddedServletContainerCustomizer {

		@Value("${endpoints.error.path:/error}")
		private String errorPath = "/error";

		@Autowired
		private ApplicationContext beanFactory;

		@Override
		public void customize(ConfigurableEmbeddedServletContainerFactory factory) {
			ManagementServerProperties configuration = this.beanFactory
					.getBean(ManagementServerProperties.class);
			factory.setPort(configuration.getPort());
			factory.setAddress(configuration.getAddress());
			factory.setContextPath(configuration.getContextPath());
			factory.addErrorPages(new ErrorPage(this.errorPath));
		}

	}

}