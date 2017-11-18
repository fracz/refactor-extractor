/*
 * Copyright 2012-2016 the original author or authors.
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

package org.springframework.boot.autoconfigure.session;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for configuring Spring Session's auto-configuration.
 *
 * @author Tommy Ludwig
 * @since 1.4.0
 */
@ConfigurationProperties("spring.session")
public class SessionProperties {

	private Store store;

	public Store getStore() {
		return this.store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	/**
	 * Session store-specific properties.
	 */
	public static class Store {
		/**
		 * Session data store type, auto-detected according to the environment by default.
		 */
		private StoreType type;

		public StoreType getType() {
			return this.type;
		}

		public void setType(StoreType type) {
			this.type = type;
		}
	}
}