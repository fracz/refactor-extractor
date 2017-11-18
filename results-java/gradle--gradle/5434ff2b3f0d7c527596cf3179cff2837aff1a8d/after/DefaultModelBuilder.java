/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.tooling.internal.consumer;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.model.Element;

import java.io.InputStream;
import java.io.OutputStream;

public class DefaultModelBuilder<T extends Element, P> extends AbstractLongRunningOperation implements ModelBuilder<T> {
    private final Class<T> modelType;
    private final Class<P> protocolType;
    private final AsyncConnection connection;
    private final ProtocolToModelAdapter adapter;
    public final static String INCOMPATIBLE_VERSION_HINT =
            "Most likely the model of that type is not supported in the target Gradle version."
            + "\nTo resolve the problem you can change/upgrade the Gradle version the tooling api connects to.";

    public DefaultModelBuilder(Class<T> modelType, Class<P> protocolType, AsyncConnection connection, ProtocolToModelAdapter adapter, ConnectionParameters parameters) {
        super(parameters);
        this.modelType = modelType;
        this.protocolType = protocolType;
        this.connection = connection;
        this.adapter = adapter;
    }

    public T get() throws GradleConnectionException {
        BlockingResultHandler<T> handler = new BlockingResultHandler<T>(modelType);
        get(handler);
        return handler.getResult();
    }

    public void get(final ResultHandler<? super T> handler) throws IllegalStateException {
        ResultHandler<P> adaptingHandler = new ProtocolToModelAdaptingHandler(handler);
        connection.getModel(protocolType, operationParameters(), new ResultHandlerAdapter<P>(adaptingHandler) {
            @Override
            protected String connectionFailureMessage(Throwable failure) {
                String message = String.format("Could not fetch model of type '%s' using %s.", modelType.getSimpleName(), connection.getDisplayName());
                if (failure instanceof UnsupportedOperationException) {
                    message += "\n" + INCOMPATIBLE_VERSION_HINT;
                }
                return message;
            }
        });
    }

    @Override
    public DefaultModelBuilder<T, P> setStandardOutput(OutputStream outputStream) {
        super.setStandardOutput(outputStream);
        return this;
    }

    @Override
    public DefaultModelBuilder<T, P> setStandardError(OutputStream outputStream) {
        super.setStandardError(outputStream);
        return this;
    }

    @Override
    public DefaultModelBuilder<T, P> setStandardInput(InputStream inputStream) {
        super.setStandardInput(inputStream);
        return this;
    }

    @Override
    public DefaultModelBuilder<T, P> addProgressListener(ProgressListener listener) {
        super.addProgressListener(listener);
        return this;
    }

    private class ProtocolToModelAdaptingHandler implements ResultHandler<P> {
        private final ResultHandler<? super T> handler;

        public ProtocolToModelAdaptingHandler(ResultHandler<? super T> handler) {
            this.handler = handler;
        }

        public void onComplete(P result) {
            handler.onComplete(adapter.adapt(modelType, result));

        }

        public void onFailure(GradleConnectionException failure) {
            handler.onFailure(failure);
        }
    }
}