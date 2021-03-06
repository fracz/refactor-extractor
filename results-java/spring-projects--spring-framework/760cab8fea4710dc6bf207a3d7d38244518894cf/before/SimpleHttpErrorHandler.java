/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.web.client.core;

import java.io.IOException;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.http.HttpStatus;
import org.springframework.web.http.client.ClientHttpResponse;

/**
 * Default implementation of the {@link HttpErrorHandler} interface.
 *
 * <p>This error handler checks for the status code on the {@link ClientHttpResponse}: any code with series
 * {@link HttpStatus.Series#CLIENT_ERROR} or {@link HttpStatus.Series#SERVER_ERROR} is considered to be an error.
 * This behavior can be changed by overriding the {@link #hasError(HttpStatus)} method.
 *
 * @author Arjen Poutsma
 * @see RestTemplate#setErrorHandler(HttpErrorHandler)
 * @since 3.0
 */
public class SimpleHttpErrorHandler implements HttpErrorHandler {

	/**
	 * Delegates to {@link #hasError(HttpStatus)} with the response status code.
	 */
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return hasError(response.getStatusCode());
	}

	/**
	 * Template method called from {@link #hasError(ClientHttpResponse)}.
	 *
	 * <p>Default implementation checks if the given status code is {@link HttpStatus.Series#CLIENT_ERROR} or {@link
	 * HttpStatus.Series#SERVER_ERROR}. Can be overridden in subclasses.
	 *
	 * @param statusCode the HTTP status code
	 * @return <code>true</code> if the response has an error; <code>false</code> otherwise
	 * @see HttpStatus.Series#CLIENT_ERROR
	 * @see HttpStatus.Series#SERVER_ERROR
	 */
	protected boolean hasError(HttpStatus statusCode) {
		return statusCode.series() == HttpStatus.Series.CLIENT_ERROR ||
				statusCode.series() == HttpStatus.Series.SERVER_ERROR;
	}

	public void handleError(ClientHttpResponse response) throws IOException {
		HttpStatus statusCode = response.getStatusCode();
		switch (statusCode.series()) {
			case CLIENT_ERROR:
				throw new HttpClientErrorException(statusCode, response.getStatusText());
			case SERVER_ERROR:
				throw new HttpServerErrorException(statusCode, response.getStatusText());
			default:
				throw new HttpClientException("Unknown status code [" + statusCode + "]");
		}
	}
}