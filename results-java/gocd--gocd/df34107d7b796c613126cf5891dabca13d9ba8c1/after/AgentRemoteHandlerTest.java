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

import com.thoughtworks.go.config.AgentConfig;
import com.thoughtworks.go.domain.AgentInstance;
import com.thoughtworks.go.domain.AgentStatus;
import com.thoughtworks.go.remote.AgentIdentifier;
import com.thoughtworks.go.remote.AgentInstruction;
import com.thoughtworks.go.remote.BuildRepositoryRemote;
import com.thoughtworks.go.server.service.AgentRuntimeInfo;
import com.thoughtworks.go.server.service.AgentService;
import com.thoughtworks.go.util.SystemEnvironment;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AgentRemoteHandlerTest implements Agent {
    private AgentRemoteHandler handler;
    private BuildRepositoryRemote remote;
    private AgentService agentService;
    private List<Message> messages = new ArrayList<>();

    @Before
    public void setUp() {
        remote = mock(BuildRepositoryRemote.class);
        agentService = mock(AgentService.class);
        handler = new AgentRemoteHandler(remote, agentService);
    }

    @Test
    public void registerConnectedAgentsByPing() {
        AgentRuntimeInfo info = AgentRuntimeInfo.fromAgent(new AgentIdentifier("HostName", "ipAddress", "uuid"));
        info.setCookie("cookie");
        AgentInstance agentInstance = AgentInstance.createFromLiveAgent(info, new SystemEnvironment());
        when(agentService.findAgent("uuid")).thenReturn(agentInstance);
        when(remote.ping(info)).thenReturn(new AgentInstruction(false));

        handler.process(this, new Message(Action.ping, info));

        verify(remote).ping(info);
        assertEquals(1, handler.connectedAgents().size());
        assertEquals(this, handler.connectedAgents().get("uuid"));
        assertTrue(messages.isEmpty());
    }

    @Test
    public void shouldCallForRegisterIfAgentInstanceIsNotRegistered() {
        AgentRuntimeInfo info = AgentRuntimeInfo.fromAgent(new AgentIdentifier("HostName", "ipAddress", "uuid"));
        info.setCookie("cookie");
        info.setStatus(AgentStatus.Pending);
        AgentInstance agentInstance = AgentInstance.createFromLiveAgent(info, new SystemEnvironment());
        when(agentService.findAgent("uuid")).thenReturn(agentInstance);

        handler.process(this, new Message(Action.ping, info));

        assertEquals(0, handler.connectedAgents().size());
        assertEquals(1, messages.size());
        assertEquals(Action.reregister, messages.get(0).getAction());
    }

    @Test
    public void shouldCancelJobIfAgentRuntimeStatusIsCanceledOnSeverSideWhenClientPingsServer() {
        AgentRuntimeInfo info = AgentRuntimeInfo.fromAgent(new AgentIdentifier("HostName", "ipAddress", "uuid"));
        info.setCookie("cookie");
        when(remote.ping(info)).thenReturn(new AgentInstruction(true));

        handler.process(this, new Message(Action.ping, info));

        verify(remote).ping(info);
        assertEquals(1, handler.connectedAgents().size());
        assertEquals(this, handler.connectedAgents().get("uuid"));

        assertEquals(1, messages.size());
        assertEquals(messages.get(0).getAction(), Action.cancelJob);
    }

    @Test
    public void shouldSetCookieIfNoCookieFoundWhenAgentPingsServer() {
        AgentIdentifier identifier = new AgentIdentifier("HostName", "ipAddress", "uuid");
        AgentRuntimeInfo info = AgentRuntimeInfo.fromAgent(identifier);
        when(remote.getCookie(identifier, info.getLocation())).thenReturn("new cookie");
        when(remote.ping(info)).thenReturn(new AgentInstruction(false));

        handler.process(this, new Message(Action.ping, info));

        verify(remote).ping(info);
        verify(remote).getCookie(identifier, info.getLocation());
        assertEquals(1, messages.size());
        assertEquals(messages.get(0).getAction(), Action.setCookie);
        assertEquals(messages.get(0).getData(), "new cookie");
    }

    @Test
    public void shouldSetCookieAndCancelJobWhenPingServerWithoutCookieAndServerSideRuntimeStatusIsCanceled() {
        AgentIdentifier identifier = new AgentIdentifier("HostName", "ipAddress", "uuid");
        AgentRuntimeInfo info = AgentRuntimeInfo.fromAgent(identifier);
        when(remote.getCookie(identifier, info.getLocation())).thenReturn("new cookie");
        when(remote.ping(info)).thenReturn(new AgentInstruction(true));

        handler.process(this, new Message(Action.ping, info));

        verify(remote).ping(info);
        assertEquals(2, messages.size());
        assertEquals(messages.get(0).getAction(), Action.setCookie);
        assertEquals(messages.get(0).getData(), "new cookie");
        assertEquals(messages.get(1).getAction(), Action.cancelJob);
    }

    @Test
    public void removeAgentShouldChangeAgentStatusToLostContest() {

    }

    @Override
    public boolean send(Message msg) {
        messages.add(msg);
        return true;
    }
}