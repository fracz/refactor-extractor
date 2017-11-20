/*
 * Copyright 2015 ThoughtWorks, Inc.
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
package com.thoughtworks.go.server.websocket;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;

@WebSocket
public class AgentRemoteSocket implements Agent {
    private static final Logger LOGGER = Logger.getLogger(AgentRemoteSocket.class);
    private AgentRemoteHandler handler;
    private Session session;

    public AgentRemoteSocket(AgentRemoteHandler handler) {
        this.handler = handler;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(sessionName() + " connected.");
        }
    }

    @OnWebSocketMessage
    public void onMessage(String json) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(sessionName() + " json: " + json);
        }
        handler.process(this, Message.decode(json));
    }

    @OnWebSocketClose
    public void onClose(int closeCode, String closeReason) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(sessionName() + " closed. code: " + closeCode + ", reason: " + closeReason);
        }
        handler.remove(this);
    }

    @OnWebSocketError
    public void onError(Throwable error) {
        LOGGER.error(sessionName() + " error", error);
    }

    @Override
    public boolean send(Action action) {
        try {
            this.session.getRemote().sendString(Message.encode(action));
            return true;
        } catch (IOException e) {
            onError(e);
            return false;
        }
    }

    private String sessionName() {
        return session == null ? "[No session initialized]" : "Session[" + session.getRemoteAddress() + "]";
    }
}