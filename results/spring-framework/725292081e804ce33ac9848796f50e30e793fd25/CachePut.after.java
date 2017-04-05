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

package org.springframework.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.Cache;
import org.springframework.core.annotation.AliasFor;

/**
 * Annotation indicating that a method (or all methods on a class) triggers
 * a {@linkplain Cache#put(Object, Object) cache put} operation.
 *
 * <p>In contrast to the {@link Cacheable @Cacheable} annotation, this annotation
 * does not cause the advised method to be skipped. Rather, it always causes the
 * method to be invoked and its result to be stored in a cache.
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 3.1
 * @see CacheConfig
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CachePut {

	/**
	 * Alias for {@link #cacheNames}.
	 */
	@AliasFor("cacheNames")
	String[] value() default {};

	/**
	 * Names of the caches to use for the cache put operation.
	 * <p>Names may be used to determine the target cache (or caches), matching
	 * the qualifier value or bean name of a specific bean definition.
	 * @since 4.2
	 * @see #value
	 * @see CacheConfig#cacheNames
	 */
	@AliasFor("value")
	String[] cacheNames() default {};

	/**
	 * Spring Expression Language (SpEL) expression for computing the key dynamically.
	 * <p>Default is {@code ""}, meaning all method parameters are considered as a key, unless
	 * a custom {@link #keyGenerator} has been set.
	 */
	String key() default "";

	/**
	 * The bean name of the custom {@link org.springframework.cache.interceptor.KeyGenerator} to use.
	 * <p>Mutually exclusive with the {@link #key} attribute.
	 * @see CacheConfig#keyGenerator
	 */
	String keyGenerator() default "";

	/**
	 * The bean name of the custom {@link org.springframework.cache.CacheManager} to use to
	 * create a default {@link org.springframework.cache.interceptor.CacheResolver} if none
	 * is set already.
	 * <p>Mutually exclusive with the {@link #cacheResolver} attribute.
	 * @see org.springframework.cache.interceptor.SimpleCacheResolver
	 * @see CacheConfig#cacheManager
	 */
	String cacheManager() default "";

	/**
	 * The bean name of the custom {@link org.springframework.cache.interceptor.CacheResolver} to use.
	 * @see CacheConfig#cacheResolver
	 */
	String cacheResolver() default "";

	/**
	 * Spring Expression Language (SpEL) expression used for making the cache
	 * put operation conditional.
	 * <p>Default is {@code ""}, meaning the method result is always cached.
	 */
	String condition() default "";

	/**
	 * Spring Expression Language (SpEL) expression used to veto the cache put operation.
	 * <p>Unlike {@link #condition}, this expression is evaluated after the method
	 * has been called and can therefore refer to the {@code result}.
	 * <p>Default is {@code ""}, meaning that caching is never vetoed.
	 * @since 3.2
	 */
	String unless() default "";

}