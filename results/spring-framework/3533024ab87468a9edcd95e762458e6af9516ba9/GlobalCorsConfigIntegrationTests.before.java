/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.reactive.result.method.annotation;

import org.junit.Before;
import org.junit.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebReactiveConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 *
 * Integration tests with {@code @RequestMapping} handler methods and global
 * CORS configuration.
 *
 * @author Sebastien Deleuze
 * @author Rossen Stoyanchev
 */
public class GlobalCorsConfigIntegrationTests extends AbstractRequestMappingIntegrationTests {

	private HttpHeaders headers;


	@Before
	public void setup() throws Exception {
		super.setup();
		this.headers = new HttpHeaders();
		this.headers.setOrigin("http://localhost:9000");
	}


	@Override
	protected ApplicationContext initApplicationContext() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(WebConfig.class);
		context.refresh();
		return context;
	}

	@Override
	protected RestTemplate initRestTemplate() {
		// JDK default HTTP client blacklists headers like Origin
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
	}


	@Test
	public void actualRequestWithCorsEnabled() throws Exception {
		ResponseEntity<String> entity = performGet("/cors", this.headers, String.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertEquals("http://localhost:9000", entity.getHeaders().getAccessControlAllowOrigin());
		assertEquals("cors", entity.getBody());
	}

	@Test
	public void actualRequestWithCorsRejected() throws Exception {
		try {
			performGet("/cors-restricted", this.headers, String.class);
			fail();
		}
		catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
		}
	}

	@Test
	public void actualRequestWithoutCorsEnabled() throws Exception {
		ResponseEntity<String> entity = performGet("/welcome", this.headers, String.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertNull(entity.getHeaders().getAccessControlAllowOrigin());
		assertEquals("welcome", entity.getBody());
	}

	@Test
	public void preFlightRequestWithCorsEnabled() throws Exception {
		this.headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
		ResponseEntity<String> entity = performOptions("/cors", this.headers, String.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
		assertEquals("http://localhost:9000", entity.getHeaders().getAccessControlAllowOrigin());
	}

	@Test
	public void preFlightRequestWithCorsRejected() throws Exception {
		try {
			this.headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
			performOptions("/cors-restricted", this.headers, String.class);
			fail();
		}
		catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
		}
	}

	@Test
	public void preFlightRequestWithoutCorsEnabled() throws Exception {
		try {
			this.headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
			performOptions("/welcome", this.headers, String.class);
			fail();
		}
		catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
		}
	}


	@Configuration
	@ComponentScan(resourcePattern = "**/GlobalCorsConfigIntegrationTests*.class")
	@SuppressWarnings({"unused", "WeakerAccess"})
	static class WebConfig extends WebReactiveConfiguration {

		@Override
		protected void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/cors-restricted").allowedOrigins("http://foo");
			registry.addMapping("/cors");
		}
	}

	@RestController @SuppressWarnings("unused")
	static class TestController {

		@GetMapping("/welcome")
		public String welcome() {
			return "welcome";
		}

		@GetMapping("/cors")
		public String cors() {
			return "cors";
		}

		@GetMapping("/cors-restricted")
		public String corsRestricted() {
			return "corsRestricted";
		}
	}

}