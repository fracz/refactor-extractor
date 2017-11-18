/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.boot.autoconfigure.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties used to configure resource handling.
 *
 * @author Phillip Webb
 * @author Brian Clozel
 * @since 1.1.0
 */
@ConfigurationProperties(prefix = "spring.resources", ignoreUnknownFields = false)
public class ResourceProperties {

	/**
	 * Cache period for the resources served by the resource handler, in seconds.
	 */
	private Integer cachePeriod;

	/**
	 * Enable default resource handling.
	 */
	private boolean addMappings = true;

	private final Chain chain = new Chain();

	public Integer getCachePeriod() {
		return this.cachePeriod;
	}

	public void setCachePeriod(Integer cachePeriod) {
		this.cachePeriod = cachePeriod;
	}

	public boolean isAddMappings() {
		return this.addMappings;
	}

	public void setAddMappings(boolean addMappings) {
		this.addMappings = addMappings;
	}

	public Chain getChain() {
		return chain;
	}

	/**
	 * Configuration for the Spring Resource Handling chain.
	 */
	public static class Chain {

		/**
		 * Enable the Spring Resource Handling chain.
		 */
		private boolean enabled = false;

		/**
		 * Enable caching in the Resource chain.
		 */
		private boolean cache = true;

		/**
		 * Enable HTML5 app cache manifest rewriting.
		 */
		private boolean html5AppCache = false;

		private Strategy strategy = new Strategy();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isCache() {
			return cache;
		}

		public void setCache(boolean cache) {
			this.cache = cache;
		}

		public Strategy getStrategy() {
			return strategy;
		}

		public boolean isHtml5AppCache() {
			return html5AppCache;
		}

		public void setHtml5AppCache(boolean html5AppCache) {
			this.html5AppCache = html5AppCache;
		}
	}

	/**
	 * Strategies for extracting and embedding a resource version in its URL path.
	 */
	public static class Strategy {

		private Fixed fixed = new Fixed();

		private Content content = new Content();

		public Fixed getFixed() {
			return fixed;
		}

		public Content getContent() {
			return content;
		}
	}

	/**
	 * Version Strategy based on content hashing.
	 */
	public static class Content {

		/**
		 * Enable the content Version Strategy.
		 */
		private boolean enabled = false;

		/**
		 * Comma-separated list of patterns to apply to the Version Strategy.
		 */
		private String[] paths = new String[]{"/**"};

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String[] getPaths() {
			return paths;
		}

		public void setPaths(String[] paths) {
			this.paths = paths;
		}
	}

	/**
	 * Version Strategy based on a fixed version string.
	 */
	public static class Fixed {

		/**
		 * Enable the fixed Version Strategy.
		 */
		private boolean enabled = false;

		/**
		 * Comma-separated list of patterns to apply to the Version Strategy.
		 */
		private String[] paths;

		/**
		 * Version string to use for the Version Strategy.
		 */
		private String version;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String[] getPaths() {
			return paths;
		}

		public void setPaths(String[] paths) {
			this.paths = paths;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}
	}
}