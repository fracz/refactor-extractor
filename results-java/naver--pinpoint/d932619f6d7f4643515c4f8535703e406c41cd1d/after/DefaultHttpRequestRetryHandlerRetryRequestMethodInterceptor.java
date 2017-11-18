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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Cached;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

/**
 * @author Minwoo Jung
 * @author jaehong.kim
 */
public class DefaultHttpRequestRetryHandlerRetryRequestMethodInterceptor implements SimpleAroundInterceptor, HttpClient4Constants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;
    private TraceContext traceContext;
    private ServiceType serviceType = HTTP_CLIENT4_INTERNAL;

    public DefaultHttpRequestRetryHandlerRetryRequestMethodInterceptor(TraceContext context, @Cached MethodDescriptor methodDescriptor) {
        setTraceContext(context);
        setMethodDescriptor(methodDescriptor);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();

        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();

        trace.recordServiceType(serviceType);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            trace.recordApi(descriptor);
            trace.recordException(throwable);

            if (args.length >=1 && (args[0] instanceof Exception)) {
                trace.recordAttribute(AnnotationKey.HTTP_CALL_RETRY_COUNT, args[0].getClass().getName());
            }
            if (result != null) {
                trace.recordAttribute(AnnotationKey.RETURN_DATA, result);
            }

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }

    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}