/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.origin;

import java.util.HashMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for {@link PropertySourceOrigin}.
 *
 * @author Phillip Webb
 */
public class PropertySourceOriginTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void createWhenPropertySourceIsNullShouldThrowException() {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("PropertySource must not be null");
		new PropertySourceOrigin(null, "name");
	}

	@Test
	public void createWhenPropertyNameIsNullShouldThrowException() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("PropertyName must not be empty");
		new PropertySourceOrigin(mock(PropertySource.class), null);
	}

	@Test
	public void createWhenPropertyNameIsEmptyShouldThrowException() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("PropertyName must not be empty");
		new PropertySourceOrigin(mock(PropertySource.class), "");
	}

	@Test
	public void getPropertySourceShouldReturnPropertySource() throws Exception {
		MapPropertySource propertySource = new MapPropertySource("test", new HashMap<>());
		PropertySourceOrigin origin = new PropertySourceOrigin(propertySource, "foo");
		assertThat(origin.getPropertySource()).isEqualTo(propertySource);
	}

	@Test
	public void getPropertyNameShouldReturnPropertyName() throws Exception {
		MapPropertySource propertySource = new MapPropertySource("test", new HashMap<>());
		PropertySourceOrigin origin = new PropertySourceOrigin(propertySource, "foo");
		assertThat(origin.getPropertyName()).isEqualTo("foo");
	}

	@Test
	public void toStringShouldShowDetails() throws Exception {
		MapPropertySource propertySource = new MapPropertySource("test", new HashMap<>());
		PropertySourceOrigin origin = new PropertySourceOrigin(propertySource, "foo");
		assertThat(origin.toString()).isEqualTo("\"foo\" from property source \"test\"");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getWhenPropertySourceSupportsOriginLookupShouldReturnOrigin()
			throws Exception {
		Origin origin = mock(Origin.class);
		PropertySource<?> propertySource = mock(PropertySource.class,
				withSettings().extraInterfaces(OriginLookup.class));
		OriginLookup<String> originCapablePropertySource = (OriginLookup<String>) propertySource;
		given(originCapablePropertySource.getOrigin("foo")).willReturn(origin);
		assertThat(PropertySourceOrigin.get(propertySource, "foo")).isSameAs(origin);
	}

	@Test
	public void getWhenPropertySourceSupportsOriginLookupButNoOriginShouldWrap()
			throws Exception {
		PropertySource<?> propertySource = mock(PropertySource.class,
				withSettings().extraInterfaces(OriginLookup.class));
		assertThat(PropertySourceOrigin.get(propertySource, "foo"))
				.isInstanceOf(PropertySourceOrigin.class);
	}

	@Test
	public void getWhenPropertySourceIsNotOriginAwareShouldWrap() throws Exception {
		MapPropertySource propertySource = new MapPropertySource("test", new HashMap<>());
		PropertySourceOrigin origin = new PropertySourceOrigin(propertySource, "foo");
		assertThat(origin.getPropertySource()).isEqualTo(propertySource);
		assertThat(origin.getPropertyName()).isEqualTo("foo");
	}

}