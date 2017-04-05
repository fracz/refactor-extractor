/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.web.converter;

import java.io.IOException;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.springframework.util.MediaType;
import org.springframework.web.http.MockHttpInputMessage;
import org.springframework.web.http.MockHttpOutputMessage;

/**
 * @author Arjen Poutsma
 */
public class ByteArrayHttpMessageConverterTests {

	private ByteArrayHttpMessageConverter converter;

	@Before
	public void setUp() {
		converter = new ByteArrayHttpMessageConverter();
	}

	@Test
	public void read() throws IOException {
		byte[] body = new byte[]{0x1, 0x2};
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body);
		inputMessage.getHeaders().setContentType(new MediaType("application", "octet-stream"));
		byte[] result = converter.read(byte[].class, inputMessage);
		assertArrayEquals("Invalid result", body, result);
	}

	@Test
	public void write() throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		byte[] body = new byte[]{0x1, 0x2};
		converter.write(body, outputMessage);
		assertArrayEquals("Invalid result", body, outputMessage.getBodyAsBytes());
		assertEquals("Invalid content-type", new MediaType("application", "octet-stream"),
				outputMessage.getHeaders().getContentType());
		assertEquals("Invalid content-length", 2, outputMessage.getHeaders().getContentLength());
	}

}