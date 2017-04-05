/*
 * Copyright 2010-2011 the original author or authors.
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

package org.springframework.cache.interceptor;

import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * Proxy factory bean for simplified declarative caching handling.
 * This is a convenient alternative to a standard AOP
 * {@link org.springframework.aop.framework.ProxyFactoryBean}
 * with a separate {@link CachingInterceptor} definition.
 *
 * <p>This class is intended to cover the <i>typical</i> case of declarative
 * cache demarcation: namely, wrapping a singleton target object with a
 * caching proxy, proxying all the interfaces that the target implements.
 *
 * @author Costin Leau
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see CachingInterceptor
 */
public class CacheProxyFactoryBean extends AbstractSingletonProxyFactoryBean {

	private final CacheInterceptor cachingInterceptor = new CacheInterceptor();
	private Pointcut pointcut;

	/**
	 * Set a pointcut, i.e a bean that can cause conditional invocation
	 * of the CacheInterceptor depending on method and attributes passed.
	 * Note: Additional interceptors are always invoked.
	 * @see #setPreInterceptors
	 * @see #setPostInterceptors
	 */
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	@Override
	protected Object createMainInterceptor() {
		this.cachingInterceptor.afterPropertiesSet();
		if (this.pointcut != null) {
			return new DefaultPointcutAdvisor(this.pointcut, this.cachingInterceptor);
		} else {
			// Rely on default pointcut.
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Set the caching attribute source which is used to find the cache operation
	 * definition.
	 *
	 * @param cacheDefinitionSources cache definition sources
	 */
	public void setCacheDefinitionSources(CacheOperationSource... cacheDefinitionSources) {
		this.cachingInterceptor.setCacheOperationSources(cacheDefinitionSources);
	}

}