/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.autoconfigure.jdbc;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * A {@link DataSourceMetadata} implementation for the commons dbcp
 * data source.
 *
 * @author Stephane Nicoll
 * @since 1.2.0
 */
public class CommonsDbcpDataSourceMetadata extends AbstractDataSourceMetadata<BasicDataSource> {

	public CommonsDbcpDataSourceMetadata(BasicDataSource dataSource) {
		super(dataSource);
	}

	@Override
	public Integer getPoolSize() {
		return getDataSource().getNumActive();
	}

	@Override
	public Integer getMaxPoolSize() {
		return getDataSource().getMaxActive();
	}

	@Override
	public Integer getMinPoolSize() {
		return getDataSource().getMinIdle();
	}

}