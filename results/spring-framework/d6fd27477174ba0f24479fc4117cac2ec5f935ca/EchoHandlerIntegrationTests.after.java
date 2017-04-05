/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.reactive.web.http;

import java.net.URI;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


@RunWith(Parameterized.class)
public class EchoHandlerIntegrationTests {

	private static final int REQUEST_SIZE = 4096 * 3;

	private static int port = SocketUtils.findAvailableTcpPort();


	@Parameterized.Parameter(0)
	public HttpServer server;

	private Random rnd = new Random();


	@Parameterized.Parameters(name = "server [{0}]")
	public static Object[][] arguments() {
		return new Object[][] {
				{new JettyHttpServer()},
				{new TomcatHttpServer()},
				{new RxNettyHttpServer()}
		};
	}


	@Before
	public void setup() throws Exception {
		this.server.setPort(port);
		this.server.setHandler(new EchoHandler());
		this.server.afterPropertiesSet();
		this.server.start();
	}

	@After
	public void tearDown() throws Exception {
		this.server.stop();
	}


	@Test
	public void echoBytes() throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		byte[] body = randomBytes();
		RequestEntity<byte[]> request = RequestEntity.post(new URI("http://localhost:" + port)).body(body);
		ResponseEntity<byte[]> response = restTemplate.exchange(request, byte[].class);

		assertArrayEquals(body, response.getBody());
	}

	@Test
	public void echoString() throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		String body = randomString();
		RequestEntity<String> request = RequestEntity.post(new URI("http://localhost:" + port)).body(body);
		ResponseEntity<String> response = restTemplate.exchange(request, String.class);

		assertEquals(body, response.getBody());
	}

	private String randomString() {
		StringBuilder builder = new StringBuilder();
		int i = 1;
		while (builder.length() < REQUEST_SIZE) {
			builder.append(randomChar());
			if (i % 5 == 0) {
				builder.append(' ');
			}
			if (i % 80 == 0) {
				builder.append('\n');
			}
			i++;
		}
		return builder.toString();
	}

	private char randomChar() {
		return (char) (rnd.nextInt(26) + 'a');
	}

	private byte[] randomBytes() {
		byte[] buffer = new byte[REQUEST_SIZE];
		rnd.nextBytes(buffer);
		return buffer;
	}

}