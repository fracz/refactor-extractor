/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Provides methods to perform networking operations, such as simple HTTP get and post requests, and TCP server/client socket
 * communication.</p>
 *
 * To perform an HTTP GET or POST request first create a {@link HttpRequest} using {@link #createHttpRequest(String)} specifying
 * the corresponding HTTP method (see {@link HttpMethods} for common methods) you want to use and then invoke the method
 * {@link #processHttpRequest(HttpRequest, HttpResponseListener)}. This will return a {@link HttpResponse} which provides methods
 * to query the progress and data returned by the operations. The {@link HttpResponse} works like a {@link Future} in that the
 * operation is executed asynchronously, while the API client can use the {@link HttpResponse} to poll for the status and result
 * of the operation.</p>
 *
 * To create a TCP client socket to communicate with a remote TCP server, invoke the
 * {@link #newClientSocket(Protocol, String, int, SocketHints)} method. The returned {@link Socket} offers an {@link InputStream}
 * and {@link OutputStream} to communicate with the end point.</p>
 *
 * To create a TCP server socket that waits for incoming connections, invoke the
 * {@link #newServerSocket(Protocol, int, ServerSocketHints)} method. The returned {@link ServerSocket} offers an
 * {@link ServerSocket#accept(SocketHints options)} method that waits for an incoming connection.
 *
 * @author mzechner
 * @author noblemaster */
public interface Net {

	/** {@link Future} like interface used with the HTTP get and post methods. Allows to cancel the operation, get it's current
	 * progress and return the result as a byte array or string. Implementations must be thread-safe.
	 *
	 * @author mzechner */
	public interface HttpResponse {
		/** @return true in case the operation was completed normally or abnormally (cancelled, exception). */
		public boolean isDone ();

		/** @return true in case the operation was cancelled or terminated abnormally, e.g. due to an exception. */
		public boolean isAborted ();

		/** @return an estimate of the progress as a number between 0.0 and 1.0. This estimate might be unreliable. */
		public float getProgress ();

		/** Cancels the operation. If the operation was already canceled or completed, this method has no effect. The operation will
		 * not block. This method may or may not work depending on the implementation of the operation. */
		public void cancel ();

		/** @param timeOut the number of milliseconds to wait before giving up, 0 to block until the operation is done
		 * @return the result as a byte array or null in case of a timeout or if the operation was canceled/terminated abnormally. */
		public byte[] getResult (int timeOut);

		/** @param timeOut the number of milliseconds to wait before giving up, 0 to block until the operation is done
		 * @return the result as a string or null in case of a timeout or if the operation was canceled/terminated abnormally. */
		public String getResultAsString (int timeOut);

		/** Returns the status code of the HTTP request in case the logic could depend on the result. */
		public int getStatusCode ();

	}

	/** The HTTP method to use with a HTTP Request. */
	public static interface HttpMethods {

		public static final String GET = "GET";
		public static final String POST = "POST";

	}

	/** Abstracts the concept of different types of HTTP Request, create it using {@link Net#createHttpRequest(String)}:
	 *
	 * <pre>
	 * HttpRequest httpGet = Gdx.net.createHttpRequest (HttpMethods.Get);
	 * httpGet.setUrl("http://somewhere.net");
	 * ...
	 * HttpResponse httpResponse = Gdx.net.processHttpRequest (httpGet);
	 * </pre> */
	public static class HttpRequest {

		private final String httpMethod;
		private String url;
		private byte[] content;
		private Map<String, String> headers;

		public HttpRequest (String httpMethod) {
			this.httpMethod = httpMethod;
			this.headers = new HashMap<String, String>();
		}

		/** Sets the URL of the HTTP request.
		 * @param url The URL to set. */
		public void setUrl (String url) {
			this.url = url;
		}

		/** Adds a header to this HTTP request. Headers definition could be found at <a
		 * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">HTTP/1.1: Header Field Definitions</a> document.
		 * @param name the name of the header.
		 * @param value the value of the header. */
		public void setHeader (String name, String value) {
			headers.put(name, value);
		}

		/** In case the HttpRequest method is POST you can set the content to send with it.
		 * @param content The content to send with the HTTP POST. */
		public void setContent (byte[] content) {
			this.content = content;
		}

		/** Returns the HTTP method of the HttpRequest. */
		public String getMethod () {
			return httpMethod;
		}

	}

	/** Listener to be able to do custom logic once the HTTP response is ready to be processed, register it with
	 * {@link Net#processHttpRequest(HttpRequest, HttpResponseListener)}.
	 * @author acoppes */
	public static interface HttpResponseListener {

		/** Called when the HTTP request has data to be processed.
		 * @param httpResponse The {@link HttpResponse} with information to be used. */
		void handleHttpResponse (HttpResponse httpResponse);

	}

	/** Returns a new {@link HttpRequest} for the given HTTP method.
	 * @param httpMethod The String representing the HTTP method for the HTTP request (see {@link HttpMethods} for common methods).
	 * @return a new instance of a {@link HttpRequest} for the given HTTP method. */
	public HttpRequest createHttpRequest (String httpMethod);

	/** Process the specified {@link HttpRequest} and reports the {@link HttpResponse} to the specified {@link HttpResponseListener}
	 * .
	 * @param httpRequest The {@link HttpRequest} to be performed.
	 * @param httpResponseListener The {@link HttpResponseListener} to call once the HTTP response is ready to be processed. Could
	 *           be null, in that case no listener is called. */
	public void processHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResponseListener);

	/** Process the specified {@link HttpRequest}.
	 * @param httpRequest The {@link HttpRequest} to be performed. */
	public void processHttpRequest (HttpRequest httpRequest);

	/** Protocol used by {@link Net#newServerSocket(Protocol, int, ServerSocketHints)} and
	 * {@link Net#newClientSocket(Protocol, String, int, SocketHints)}.
	 * @author mzechner */
	public enum Protocol {
		TCP
	}

	/** Creates a new server socket on the given port, using the given {@link Protocol}, waiting for incoming connections.
	 *
	 * @param port the port to listen on
	 * @param hints additional {@link ServerSocketHints} used to create the socket. Input null to use the default setting provided
	 *           by the system.
	 * @return the {@link ServerSocket}
	 * @throws GdxRuntimeException in case the socket couldn't be opened */
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints);

	/** Creates a new TCP client socket that connects to the given host and port.
	 *
	 * @param host the host address
	 * @param port the port
	 * @param hints additional {@link SocketHints} used to create the socket. Input null to use the default setting provided by the
	 *           system.
	 * @return GdxRuntimeException in case the socket couldn't be opened */
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints);

	/** Launches the default browser to display a URI. If the default browser is not able to handle the specified URI, the
	 * application registered for handling URIs of the specified type is invoked. The application is determined from the protocol
	 * and path of the URI.
	 *
	 * @param URI the URI to be opened. */
	public void openURI (String URI);
}