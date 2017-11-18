/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.modifier.arcus;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.ParameterExtractorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.ArcusScope;
import com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.IndexParameterExtractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author harebox
 */
public class FrontCacheMemcachedClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(FrontCacheMemcachedClientModifier.class.getName());

    public FrontCacheMemcachedClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName,
                         ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            String[] args = {"java.lang.String", "java.util.concurrent.Future", Long.TYPE.toString()};
            if (!checkCompatibility(aClass, args)) {
                return null;
            }

            // Inject ApiInterceptor to all public methods.
            final List<MethodInfo> declaredMethods = aClass.getDeclaredMethods(new FrontCacheMemcachedMethodFilter());

            for (MethodInfo method : declaredMethods) {
                SimpleAroundInterceptor apiInterceptor = (SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.ApiInterceptor");
                if (this.getProfilerConfig().isMemcachedKeyTrace()) {
                    final int index = ParameterUtils.findFirstString(method, 3);
                    if (index != -1) {
                        ((ParameterExtractorSupport) apiInterceptor).setParameterExtractor(new IndexParameterExtractor(index));
                    }
                }
                aClass.addGroupInterceptor(method.getName(), method.getParameterTypes(), apiInterceptor, ArcusScope.SCOPE);
            }
            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    private boolean checkCompatibility(InstrumentClass aClass, String[] args) {
        final boolean putFrontCache = aClass.hasDeclaredMethod("putFrontCache", args);
        if (!putFrontCache) {
            logger.warn("putFrontCache() not found. skip FrontCacheMemcachedClientModifier");
        }
        return putFrontCache;
    }

    public String getTargetClass() {
        return "net/spy/memcached/plugin/FrontCacheMemcachedClient";
    }
}