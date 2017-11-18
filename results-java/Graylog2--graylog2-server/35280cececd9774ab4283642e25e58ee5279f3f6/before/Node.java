/**
 * Copyright 2013 Lennart Koopmann <lennart@torch.sh>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package models;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import lib.APIException;
import lib.ApiClient;
import models.api.responses.BuffersResponse;
import models.api.responses.NodeSummaryResponse;
import models.api.responses.system.InputSummaryResponse;
import models.api.responses.system.InputsResponse;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import play.Logger;

import java.io.IOException;
import java.util.List;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class Node {

    public interface Factory {
        Node fromSummaryResponse(NodeSummaryResponse r);
    }

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Node.class);
    private final ApiClient api;
    private final String transportAddress;
    private final DateTime lastSeen;

    private final String nodeId;
    private final String shortNodeId;
    private final String hostname;
    private final boolean isMaster;
    private final boolean isProcessing;

    @AssistedInject
    public Node(ApiClient api, @Assisted NodeSummaryResponse r) {
        this.api = api;

        transportAddress = r.transportAddress;
        lastSeen = new DateTime(r.lastSeen);
        nodeId = r.nodeId;
        shortNodeId = r.shortNodeId;
        hostname = r.hostname;
        isMaster = r.isMaster;
        isProcessing = r.isProcessing;
    }

    public BufferInfo getBufferInfo() {
        try {
            return new BufferInfo(
                    api.get(BuffersResponse.class)
                    .node(this)
                    .path("/system/buffers")
                    .execute());
        } catch (APIException e) {
            log.error("Unable to read buffer info from node " + this, e);
        } catch (IOException e) {
            log.error("Unexpected exception", e);
        }
        return null;
    }

    public String getThreadDump() throws IOException, APIException {
        return api.get(String.class).node(this).path("/system/threaddump").execute();
    }

    public List<Input> getInputs() {
        List<Input> inputs = Lists.newArrayList();

        for (InputSummaryResponse input : inputs().inputs) {
            inputs.add(new Input(input));
        }

        return inputs;
    }

    public Input getInput(String inputId) throws IOException, APIException {
        final InputSummaryResponse inputSummaryResponse = api.get(InputSummaryResponse.class).node(this).path("/system/inputs/{0}", inputId).execute();
        return new Input(inputSummaryResponse);
    }

    public int numberOfInputs() {
        return inputs().total;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public DateTime getLastSeen() {
        return lastSeen;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getShortNodeId() {
        return shortNodeId;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public void pause() throws IOException, APIException {
        api.put()
            .path("/system/processing/pause")
            .node(this)
            .execute();
    }

    public void resume() throws IOException, APIException {
        api.put()
            .path("/system/processing/resume")
            .node(this)
            .execute();
    }

    /**
     * This swallows all exceptions to allow easy lazy-loading in views without exception handling.
     *
     * @return List of running inputs o this node.
     */
    private InputsResponse inputs() {
        try {
            return api.get(InputsResponse.class).node(this).path("/system/inputs").execute();
        } catch (Exception e) {
            Logger.error("Could not get inputs.", e);
            throw new RuntimeException("Could not get inputs.", e);
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "nodeId='" + nodeId + '\'' +
                ", transportAddress='" + transportAddress + '\'' +
                ", isMaster=" + isMaster +
                '}';
    }
}