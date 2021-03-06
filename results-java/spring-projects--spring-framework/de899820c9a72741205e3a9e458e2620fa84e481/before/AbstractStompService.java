/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.messaging.stomp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompMessage;

import reactor.Fn;
import reactor.core.Reactor;
import reactor.fn.Consumer;
import reactor.fn.Event;


/**
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public abstract class AbstractStompService {

	protected final Log logger = LogFactory.getLog(getClass());


	private final Reactor reactor;

	private final List<String> allowedDestinations = new ArrayList<String>();

	private final List<String> disallowedDestinations = new ArrayList<String>();

	private final PathMatcher pathMatcher = new AntPathMatcher();


	public AbstractStompService(Reactor reactor) {

		Assert.notNull(reactor, "reactor is required");
		this.reactor = reactor;

		this.reactor.on(Fn.$(StompCommand.CONNECT), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				processConnect(event.getData(), event.getReplyTo());
			}
		});
		this.reactor.on(Fn.$(StompCommand.SUBSCRIBE), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				StompMessage message = event.getData();
				if (isAllowedDestination(message)) {
					processSubscribe(event.getData(), event.getReplyTo());
				}
			}
		});
		this.reactor.on(Fn.$(StompCommand.SEND), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				StompMessage message = event.getData();
				if (isAllowedDestination(message)) {
					processSend(event.getData());
				}
			}
		});
		this.reactor.on(Fn.$(StompCommand.DISCONNECT), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				processDisconnect(event.getData());
			}
		});
		this.reactor.on(Fn.$(StompCommand.ACK), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				processAck(event.getData());
			}
		});
		this.reactor.on(Fn.$(StompCommand.NACK), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				processNack(event.getData());
			}
		});
		this.reactor.on(Fn.$(StompCommand.BEGIN), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				processBegin(event.getData());
			}
		});
		this.reactor.on(Fn.$(StompCommand.COMMIT), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				processCommit(event.getData());
			}
		});
		this.reactor.on(Fn.$(StompCommand.ABORT), new Consumer<Event<StompMessage>>() {
			@Override
			public void accept(Event<StompMessage> event) {
				processAbort(event.getData());
			}
		});
		this.reactor.on(Fn.$("CONNECTION_CLOSED"), new Consumer<Event<String>>() {
			@Override
			public void accept(Event<String> event) {
				processConnectionClosed(event.getData());
			}
		});
	}


	/**
	 * Ant-style destination patterns that this STOMP service is allowed to process.
	 */
	public void setAllowedDestinations(String... patterns) {
		this.allowedDestinations.clear();
		this.allowedDestinations.addAll(Arrays.asList(patterns));
	}

	/**
	 * Ant-style destination patterns that this STOMP service should skip.
	 */
	public void setDisallowedDestinations(String... patterns) {
		this.disallowedDestinations.clear();
		this.disallowedDestinations.addAll(Arrays.asList(patterns));
	}

	public Reactor getReactor() {
		return this.reactor;
	}

	private boolean isAllowedDestination(StompMessage message) {
		String destination = message.getHeaders().getDestination();
		if (destination == null) {
			return true;
		}
		if (!this.disallowedDestinations.isEmpty()) {
			for (String pattern : this.disallowedDestinations) {
				if (this.pathMatcher.match(pattern, destination)) {
					return false;
				}
			}
		}
		if (!this.allowedDestinations.isEmpty()) {
			for (String pattern : this.allowedDestinations) {
				if (this.pathMatcher.match(pattern, destination)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	protected void processConnect(StompMessage message, Object replyTo) {
	}

	protected void processSubscribe(StompMessage message, Object replyTo) {
	}

	protected void processSend(StompMessage message) {
	}

	protected void processDisconnect(StompMessage message) {
	}

	protected void processAck(StompMessage message) {
	}

	protected void processNack(StompMessage message) {
	}

	protected void processBegin(StompMessage message) {
	}

	protected void processCommit(StompMessage message) {
	}

	protected void processAbort(StompMessage message) {
	}

	protected void processConnectionClosed(String sessionId) {
	}

}