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

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.test.TestSubscriber;
import rx.Observable;
import rx.Single;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.convert.support.MonoToCompletableFutureConverter;
import org.springframework.core.convert.support.ReactorToRxJava1Converter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.reactive.CodecHttpMessageConverter;
import org.springframework.http.converter.reactive.HttpMessageConverter;
import org.springframework.http.server.reactive.MockServerHttpRequest;
import org.springframework.http.server.reactive.MockServerHttpResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.reactive.result.ResolvableMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.session.MockWebSessionManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.ResolvableType.forClassWithGenerics;

/**
 * Unit tests for {@link HttpEntityArgumentResolver}.When adding a test also
 * consider whether the logic under test is in a parent class, then see:
 * {@link MessageConverterArgumentResolverTests}.
 *
 * @author Rossen Stoyanchev
 */
public class HttpEntityArgumentResolverTests {

	private HttpEntityArgumentResolver resolver = resolver();

	private ServerWebExchange exchange;

	private MockServerHttpRequest request;

	private ResolvableMethod testMethod = ResolvableMethod.on(getClass()).name("handle");


	@Before
	public void setUp() throws Exception {
		this.request = new MockServerHttpRequest(HttpMethod.POST, new URI("/path"));
		MockServerHttpResponse response = new MockServerHttpResponse();
		this.exchange = new DefaultServerWebExchange(this.request, response, new MockWebSessionManager());
	}


	@Test
	public void supports() throws Exception {
		testSupports(httpEntity(String.class));
		testSupports(httpEntity(forClassWithGenerics(Mono.class, String.class)));
		testSupports(httpEntity(forClassWithGenerics(Single.class, String.class)));
		testSupports(httpEntity(forClassWithGenerics(CompletableFuture.class, String.class)));
		testSupports(httpEntity(forClassWithGenerics(Flux.class, String.class)));
		testSupports(httpEntity(forClassWithGenerics(Observable.class, String.class)));
		testSupports(forClassWithGenerics(RequestEntity.class, String.class));
	}

	@Test
	public void doesNotSupport() throws Exception {
		ResolvableType type = ResolvableType.forClassWithGenerics(Mono.class, String.class);
		assertFalse(this.resolver.supportsParameter(this.testMethod.resolveParam(type)));

		type = ResolvableType.forClass(String.class);
		assertFalse(this.resolver.supportsParameter(this.testMethod.resolveParam(type)));
	}

	@Test
	public void httpEntityWithStringBody() throws Exception {
		String body = "line1";
		ResolvableType type = httpEntity(String.class);
		HttpEntity<String> httpEntity = resolveValue(type, body);

		assertEquals(this.request.getHeaders(), httpEntity.getHeaders());
		assertEquals("line1", httpEntity.getBody());
	}

	@Test
	public void httpEntityWithMonoBody() throws Exception {
		String body = "line1";
		ResolvableType type = httpEntity(forClassWithGenerics(Mono.class, String.class));
		HttpEntity<Mono<String>> httpEntity = resolveValue(type, body);

		assertEquals(this.request.getHeaders(), httpEntity.getHeaders());
		assertEquals("line1", httpEntity.getBody().block());
	}

	@Test
	public void httpEntityWithSingleBody() throws Exception {
		String body = "line1";
		ResolvableType type = httpEntity(forClassWithGenerics(Single.class, String.class));
		HttpEntity<Single<String>> httpEntity = resolveValue(type, body);

		assertEquals(this.request.getHeaders(), httpEntity.getHeaders());
		assertEquals("line1", httpEntity.getBody().toBlocking().value());
	}

	@Test
	public void httpEntityWithCompletableFutureBody() throws Exception {
		String body = "line1";
		ResolvableType type = httpEntity(forClassWithGenerics(CompletableFuture.class, String.class));
		HttpEntity<CompletableFuture<String>> httpEntity = resolveValue(type, body);

		assertEquals(this.request.getHeaders(), httpEntity.getHeaders());
		assertEquals("line1", httpEntity.getBody().get());
	}

	@Test
	public void httpEntityWithFluxBody() throws Exception {
		String body = "line1\nline2\nline3\n";
		ResolvableType type = httpEntity(forClassWithGenerics(Flux.class, String.class));
		HttpEntity<Flux<String>> httpEntity = resolveValue(type, body);

		assertEquals(this.request.getHeaders(), httpEntity.getHeaders());
		TestSubscriber.subscribe(httpEntity.getBody()).assertValues("line1\n", "line2\n", "line3\n");
	}

	@Test
	public void requestEntity() throws Exception {
		String body = "line1";
		ResolvableType type = forClassWithGenerics(RequestEntity.class, String.class);
		RequestEntity<String> requestEntity = resolveValue(type, body);

		assertEquals(this.request.getMethod(), requestEntity.getMethod());
		assertEquals(this.request.getURI(), requestEntity.getUrl());
		assertEquals(this.request.getHeaders(), requestEntity.getHeaders());
		assertEquals("line1", requestEntity.getBody());
	}


	private ResolvableType httpEntity(Class<?> bodyType) {
		return httpEntity(ResolvableType.forClass(bodyType));
	}

	private ResolvableType httpEntity(ResolvableType type) {
		return forClassWithGenerics(HttpEntity.class, type);
	}

	private HttpEntityArgumentResolver resolver() {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new CodecHttpMessageConverter<>(new StringDecoder()));

		FormattingConversionService service = new DefaultFormattingConversionService();
		service.addConverter(new MonoToCompletableFutureConverter());
		service.addConverter(new ReactorToRxJava1Converter());

		return new HttpEntityArgumentResolver(converters, service);
	}

	private void testSupports(ResolvableType type) {
		MethodParameter parameter = this.testMethod.resolveParam(type);
		assertTrue(this.resolver.supportsParameter(parameter));
	}

	@SuppressWarnings("unchecked")
	private <T> T resolveValue(ResolvableType type, String body) {

		this.request.getHeaders().add("foo", "bar");
		this.request.getHeaders().setContentType(MediaType.TEXT_PLAIN);
		this.request.writeWith(Flux.just(dataBuffer(body)));

		MethodParameter param = this.testMethod.resolveParam(type);
		Mono<Object> result = this.resolver.resolveArgument(param, new ExtendedModelMap(), this.exchange);
		Object value = result.block(Duration.ofSeconds(5));

		assertNotNull(value);
		assertTrue("Unexpected return value type: " + value.getClass(),
				param.getParameterType().isAssignableFrom(value.getClass()));

		return (T) value;
	}

	private DataBuffer dataBuffer(String body) {
		byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		return new DefaultDataBufferFactory().wrap(byteBuffer);
	}


	@SuppressWarnings("unused")
	void handle(
			String string,
			Mono<String> monoString,
			HttpEntity<String> httpEntity,
			HttpEntity<Mono<String>> monoBody,
			HttpEntity<Single<String>> singleBody,
			HttpEntity<CompletableFuture<String>> completableFutureBody,
			HttpEntity<Flux<String>> fluxBody,
			HttpEntity<Observable<String>> observableBody,
			RequestEntity<String> requestEntity) {}

}