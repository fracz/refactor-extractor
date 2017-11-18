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
package com.navercorp.pinpoint.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConstructorTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;

/**
 *
 * @author jaehong.kim
 *
 */
public class HttpClient4Plugin implements ProfilerPlugin, HttpClient4Constants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClient4PluginConfig config = new HttpClient4PluginConfig(context.getConfig());

        // if (config.isApacheHttpClient4Profile()) {
        // addHttpClient4ClassEditor(context, config);
        // addDefaultHttpRequestRetryHandlerClassEditor(context, config);
        // }

        addClosableHttpAsyncClientClassEditor(context, config);
        addDefaultClientExchangeHandlerImplConstructorInterceptor(context, config);
        // addClosableHttpClientClassEditor(context, config);
        addBasicFutureClassEditor(context, config);
    }

    private void addHttpClient4ClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.client.AbstractHttpClient");

        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void injectHttpRequestExecuteMethodInterceptor(final ClassFileTransformerBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpRequestExecuteInterceptor");
    }

    private void injectHttpUriRequestExecuteInterceptor(final ClassFileTransformerBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpUriRequestExecuteInterceptor");
    }

    private void addDefaultHttpRequestRetryHandlerClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.client.DefaultHttpRequestRetryHandler");
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("retryRequest", "java.io.IOException", "int", "org.apache.http.protocol.HttpContext");
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.RetryRequestInterceptor");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addClosableHttpAsyncClientClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.nio.client.CloseableHttpAsyncClient");
        // with HttpRequest
        injectCloseableHttpAsyncClientExecuteMethodWithHttpRequestInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");
        injectCloseableHttpAsyncClientExecuteMethodWithHttpRequestInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.concurrent.FutureCallback");
        // with HttpAsyncRequestProducer
        injectCloseableHttpAsyncClientExecuteMethodWithHttpAsyncRequestProducerInterceptor(classEditorBuilder, "org.apache.http.nio.protocol.HttpAsyncRequestProducer", "org.apache.http.nio.protocol.HttpAsyncResponseConsumer", "org.apache.http.concurrent.FutureCallback");
        // with HttpUriRequest
        injectCloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.concurrent.FutureCallback");
        injectCloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void injectCloseableHttpAsyncClientExecuteMethodWithHttpRequestInterceptor(final ClassFileTransformerBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.CloseableHttpAsyncClientExecuteMethodWithHttpRequestInterceptor");
    }

    private void injectCloseableHttpAsyncClientExecuteMethodWithHttpAsyncRequestProducerInterceptor(final ClassFileTransformerBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.CloseableHttpAsyncClientExecuteMethodWithHttpAsyncRequestProducerInterceptor");
    }

    private void injectCloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor(final ClassFileTransformerBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.CloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor");
    }


    private void addDefaultClientExchangeHandlerImplConstructorInterceptor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl");
        ConstructorTransformerBuilder constructorEditorBuilder = classEditorBuilder.editConstructor("org.apache.commons.logging.Log", "org.apache.http.nio.protocol.HttpAsyncRequestProducer", "org.apache.http.nio.protocol.HttpAsyncResponseConsumer",
                "org.apache.http.client.protocol.HttpClientContext", "org.apache.http.concurrent.BasicFuture", "org.apache.http.nio.conn.NHttpClientConnectionManager", "org.apache.http.impl.nio.client.InternalClientExec");
        constructorEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.DefaultClientExchangeHandlerImplConstructorInterceptor");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addClosableHttpClientClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.client.CloseableHttpClient");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addBasicFutureClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.concurrent.BasicFuture");
        classEditorBuilder.injectMetadata(METADATA_ASYNC_TRACE_ID);

        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("get");
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodTransformerBuilder getMethodEditorBuilder = classEditorBuilder.editMethod("get", "long", "java.util.concurrent.TimeUnit");
        getMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        getMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodTransformerBuilder completedMethodEditorBuilder = classEditorBuilder.editMethod("completed", "java.lang.Object");
        completedMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        completedMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodTransformerBuilder failMethodEditorBuilder = classEditorBuilder.editMethod("failed", "java.lang.Exception");
        failMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        failMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodTransformerBuilder cancelMethodEditorBuilder = classEditorBuilder.editMethod("cancel", "boolean");
        cancelMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        cancelMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        context.addClassFileTransformer(classEditorBuilder.build());
    }
}