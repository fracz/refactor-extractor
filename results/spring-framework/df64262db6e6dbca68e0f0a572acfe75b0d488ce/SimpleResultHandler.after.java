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

package org.springframework.web.reactive.result;

import java.util.Optional;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.server.ServerWebExchange;


/**
 * A simple handler for return values of type {@code void}, or
 * {@code Publisher<Void>}, or if a {link ConversionService} is provided, also
 * of any other async return value types that can be converted to
 * {@code Publisher<Void>} such as {@code Observable<Void>} or
 * {@code CompletableFuture<Void>}.
 *
 * @author Sebastien Deleuze
 * @author Rossen Stoyanchev
 */
public class SimpleResultHandler implements Ordered, HandlerResultHandler {

	private ConversionService conversionService;

	private int order = Ordered.LOWEST_PRECEDENCE;


	public SimpleResultHandler(ConversionService conversionService) {
		Assert.notNull(conversionService, "'conversionService' is required.");
		this.conversionService = conversionService;
	}


	/**
	 * Return the configured {@link ConversionService}.
	 */
	public ConversionService getConversionService() {
		return this.conversionService;
	}

	/**
	 * Set the order for this result handler relative to others.
	 * <p>By default this is set to {@link Ordered#LOWEST_PRECEDENCE} and is
	 * generally safe to use late in the order since it looks specifically for
	 * {@code void} or async return types parameterized by {@code void}.
	 * @param order the order
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}


	@Override
	public boolean supports(HandlerResult result) {
		ResolvableType type = result.getReturnValueType();
		if (Void.TYPE.equals(type.getRawClass())) {
			return true;
		}
		if (getConversionService().canConvert(type.getRawClass(), Mono.class) ||
				getConversionService().canConvert(type.getRawClass(), Flux.class)) {
			Class<?> clazz = result.getReturnValueType().getGeneric(0).getRawClass();
			return Void.class.equals(clazz);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
		Optional<Object> optional = result.getReturnValue();
		if (!optional.isPresent()) {
			return Mono.empty();
		}

		Object returnValue = optional.get();
		if (returnValue instanceof Mono) {
			return (Mono<Void>) returnValue;
		}

		ResolvableType returnType = result.getReturnValueType();
		if (getConversionService().canConvert(returnType.getRawClass(), Mono.class)) {
			return this.conversionService.convert(returnValue, Mono.class);
		}
		else {
			return this.conversionService.convert(returnValue, Flux.class).single();
		}
	}

}