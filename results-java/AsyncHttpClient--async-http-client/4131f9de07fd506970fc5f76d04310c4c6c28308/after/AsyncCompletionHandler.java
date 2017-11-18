/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package com.ning.http.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.ning.http.client.logging.LogManager;
import com.ning.http.client.logging.Logger;

/**
 * An {@link AsyncHandler} augmented with an {@link #onCompleted(Response)} convenience method which gets called
 * when the {@link Response} has been fully received.
 *
 * <strong>NOTE:<strong> Sending another asynchronous request from an {@link AsyncHandler} must be done using
 * another thread to avoid potential deadlock inside the {@link com.ning.http.client.AsyncHttpProvider}
 *
 * The recommended way is to use the {@link java.util.concurrent.ExecutorService} from the {@link com.ning.http.client.AsyncHttpClientConfig}:
 * {@code
 *         &#64;Override
 *         public T onCompleted(Response response) throws Exception
 *         &#123;
 *             asyncHttpClient.getConfig().executorService().execute(new Runnable()
 *             &#123;
 *                 public void run()
 *                 &#123;
 *                     asyncHttpClient.prepareGet(...);
 *                 &#125;
 *             &#125;);
 *            return T;
 *         &#125;
 * }
 *
 * @param <T>  Type of the value that will be returned by the associated {@link java.util.concurrent.Future}
 */
public abstract class AsyncCompletionHandler<T> implements AsyncHandler<T>, ProgressAsyncHandler<T> {

    private final Logger log = LogManager.getLogger(AsyncCompletionHandlerBase.class);
    private final Response.ResponseBuilder builder = new Response.ResponseBuilder();

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
        builder.accumulate(content);
        return STATE.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public final STATE onStatusReceived(final HttpResponseStatus status) throws Exception {
        builder.accumulate(status);
        return STATE.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public final STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
        builder.accumulate(headers);
        return STATE.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public final T onCompleted() throws Exception {
        return onCompleted(builder.build());
    }

    /**
     * {@inheritDoc}
     */
    /* @Override */
    public void onThrowable(Throwable t) {
        log.debug(t);
    }

    /**
     * Invoked once the HTTP response has been fully read.
     *
     * @param response The {@link Response}
     * @return Type of the value that will be returned by the associated {@link java.util.concurrent.Future}
     */
    abstract public T onCompleted(Response response) throws Exception;

    /**
     * Invoked when the content (a {@link java.io.File}, {@link String} or {@link java.io.FileInputStream} has been fully
     * written on the I/O socket.
     *
     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     */
    public STATE onHeaderWriteCompleted() {
        return STATE.CONTINUE;
    }

    /**
     * Invoked when the content (a {@link java.io.File}, {@link String} or {@link java.io.FileInputStream} has been fully
     * written on the I/O socket.
     *
     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     */
    public STATE onContentWriteCompleted() {
        return STATE.CONTINUE;
    }

    /**
     * Invoked when the I/O operation associated with the {@link Request} body as been progressed.
     * @param amount The amount of bytes to transfer.
     * @param current The amount of bytes transferred
     * @param total The total number of bytes transferred
     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     */
    public STATE onContentWriteProgress(long amount, long current, long total) {
        return STATE.CONTINUE;
    }
}