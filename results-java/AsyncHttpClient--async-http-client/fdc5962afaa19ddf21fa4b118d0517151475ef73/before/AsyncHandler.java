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
 */
package com.ning.http.client;

/**
 * An asynchronous handler or callback which gets invoked as soon as some data are available when
 * processing an asynchronous response. Interrupting the process of the asynchronous response can be achieved by
 * throwing an {@link com.ning.http.client.AsyncHandler.ResponseCompleted} exception at any moment during the
 * processing of the asynchronous response.
 */
public interface AsyncHandler<T> {

    /**
     * Convenience exception to throw for interrupting the processing of the asynchronous response.
     */
    public class ResponseCompleted extends Exception{
        public ResponseCompleted(){
        }
    }

    /**
     * Invoked when an unexpected exception occurs during the processing of the response
     *
     * @param t a {@link Throwable}
     */
    void onThrowable(Throwable t);

    /**
     * Invoked as soon as some response body are received.
     * @param bodyPart response's body part.
     * @throws Exception
     */
    void onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception;

    /**
     * Invoked as soon as the HTTP status line has been received
     * @param responseStatus
     * @throws Exception
     */
    void onStatusReceived(HttpResponseStatus responseStatus) throws Exception;

    /**
     * Invoked as soon as the HTTP headers has been received.
     * @param headers
     * @throws Exception
     */
    void onHeadersReceived(HttpResponseHeaders headers) throws Exception;

    /**
     * Invoked once the HTTP response has been fully received
     * @return
     * @throws Exception
     */
    T onCompleted() throws Exception;
}