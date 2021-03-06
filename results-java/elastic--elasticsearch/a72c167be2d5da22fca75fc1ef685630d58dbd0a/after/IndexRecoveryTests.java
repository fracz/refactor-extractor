/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.indices.recovery;

import com.carrotsearch.randomizedtesting.LifecycleScope;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.action.admin.indices.recovery.RecoveryResponse;
import org.elasticsearch.action.admin.indices.recovery.ShardRecoveryResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.cluster.routing.allocation.command.MoveAllocationCommand;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.recovery.RecoveryState.Stage;
import org.elasticsearch.indices.recovery.RecoveryState.Type;
import org.elasticsearch.snapshots.SnapshotState;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.ElasticsearchIntegrationTest.ClusterScope;
import org.elasticsearch.test.junit.annotations.TestLogging;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.test.ElasticsearchIntegrationTest.Scope;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.*;

/**
 *
 */
@ClusterScope(scope = Scope.TEST, numDataNodes = 0)
public class IndexRecoveryTests extends ElasticsearchIntegrationTest {

    private static final String INDEX_NAME = "test-idx-1";
    private static final String INDEX_TYPE = "test-type-1";
    private static final String REPO_NAME = "test-repo-1";
    private static final String SNAP_NAME = "test-snap-1";

    private static final int MIN_DOC_COUNT = 500;
    private static final int MAX_DOC_COUNT = 1000;
    private static final int SHARD_COUNT = 1;
    private static final int REPLICA_COUNT = 0;


    private void assertRecoveryStateWithoutStage(RecoveryState state, int shardId, Type type,
                                                 String sourceNode, String targetNode, boolean hasRestoreSource) {
        assertThat(state.getShardId().getId(), equalTo(shardId));
        assertThat(state.getType(), equalTo(type));
        if (sourceNode == null) {
            assertNull(state.getSourceNode());
        } else {
            assertNotNull(state.getSourceNode());
            assertThat(state.getSourceNode().getName(), equalTo(sourceNode));
        }
        if (targetNode == null) {
            assertNull(state.getTargetNode());
        } else {
            assertNotNull(state.getTargetNode());
            assertThat(state.getTargetNode().getName(), equalTo(targetNode));
        }
        if (hasRestoreSource) {
            assertNotNull(state.getRestoreSource());
        } else {
            assertNull(state.getRestoreSource());
        }

    }

    private void assertRecoveryState(RecoveryState state, int shardId, Type type, Stage stage,
                                     String sourceNode, String targetNode, boolean hasRestoreSource) {
        assertRecoveryStateWithoutStage(state, shardId, type, sourceNode, targetNode, hasRestoreSource);
        assertThat(state.getStage(), equalTo(stage));
    }

    private void assertOnGoingRecoveryState(RecoveryState state, int shardId, Type type,
                                            String sourceNode, String targetNode, boolean hasRestoreSource) {
        assertRecoveryStateWithoutStage(state, shardId, type, sourceNode, targetNode, hasRestoreSource);
        assertThat(state.getStage(), not(equalTo(Stage.DONE)));
    }

    private void slowDownRecovery() {
        assertTrue(client().admin().cluster().prepareUpdateSettings()
                .setTransientSettings(ImmutableSettings.builder()
                        // let the default file chunk wait 2 seconds, not to delay the test for too long
                        .put(RecoverySettings.INDICES_RECOVERY_MAX_BYTES_PER_SEC, "256kb"))
                .get().isAcknowledged());
    }

    private void restoreRecoverySpeed() {
        assertTrue(client().admin().cluster().prepareUpdateSettings()
                .setTransientSettings(ImmutableSettings.builder()
                        .put(RecoverySettings.INDICES_RECOVERY_MAX_BYTES_PER_SEC, "20mb"))
                .get().isAcknowledged());
    }

    @Test
    public void gatewayRecoveryTest() throws Exception {
        logger.info("--> start nodes");
        String node = internalCluster().startNode(settingsBuilder().put("gateway.type", "local"));

        createAndPopulateIndex(INDEX_NAME, 1, SHARD_COUNT, REPLICA_COUNT);

        logger.info("--> restarting cluster");
        internalCluster().fullRestart();
        ensureGreen();

        logger.info("--> request recoveries");
        RecoveryResponse response = client().admin().indices().prepareRecoveries(INDEX_NAME).execute().actionGet();
        assertThat(response.shardResponses().size(), equalTo(SHARD_COUNT));
        assertThat(response.shardResponses().get(INDEX_NAME).size(), equalTo(1));

        List<ShardRecoveryResponse> shardResponses = response.shardResponses().get(INDEX_NAME);
        assertThat(shardResponses.size(), equalTo(1));

        ShardRecoveryResponse shardResponse = shardResponses.get(0);
        RecoveryState state = shardResponse.recoveryState();

        assertRecoveryState(state, 0, Type.GATEWAY, Stage.DONE, node, node, false);

        validateIndexRecoveryState(state.getIndex());
    }

    @Test
    public void gatewayRecoveryTestActiveOnly() throws Exception {
        logger.info("--> start nodes");
        internalCluster().startNode(settingsBuilder().put("gateway.type", "local"));

        createAndPopulateIndex(INDEX_NAME, 1, SHARD_COUNT, REPLICA_COUNT);

        logger.info("--> restarting cluster");
        internalCluster().fullRestart();
        ensureGreen();

        logger.info("--> request recoveries");
        RecoveryResponse response = client().admin().indices().prepareRecoveries(INDEX_NAME).setActiveOnly(true).execute().actionGet();

        List<ShardRecoveryResponse> shardResponses = response.shardResponses().get(INDEX_NAME);
        assertThat(shardResponses.size(), equalTo(0));  // Should not expect any responses back
    }

    @Test
    public void replicaRecoveryTest() throws Exception {
        logger.info("--> start node A");
        String nodeA = internalCluster().startNode(settingsBuilder().put("gateway.type", "local"));

        logger.info("--> create index on node: {}", nodeA);
        createAndPopulateIndex(INDEX_NAME, 1, SHARD_COUNT, REPLICA_COUNT);

        logger.info("--> start node B");
        String nodeB = internalCluster().startNode(settingsBuilder().put("gateway.type", "local"));
        ensureGreen();

        // force a shard recovery from nodeA to nodeB
        logger.info("--> bump replica count");
        client().admin().indices().prepareUpdateSettings(INDEX_NAME)
                .setSettings(settingsBuilder().put("number_of_replicas", 1)).execute().actionGet();
        ensureGreen();

        logger.info("--> request recoveries");
        RecoveryResponse response = client().admin().indices().prepareRecoveries(INDEX_NAME).execute().actionGet();

        // we should now have two total shards, one primary and one replica
        List<ShardRecoveryResponse> shardResponses = response.shardResponses().get(INDEX_NAME);
        assertThat(shardResponses.size(), equalTo(2));

        List<ShardRecoveryResponse> nodeAResponses = findRecoveriesForTargetNode(nodeA, shardResponses);
        assertThat(nodeAResponses.size(), equalTo(1));
        List<ShardRecoveryResponse> nodeBResponses = findRecoveriesForTargetNode(nodeB, shardResponses);
        assertThat(nodeBResponses.size(), equalTo(1));

        // validate node A recovery
        ShardRecoveryResponse nodeAShardResponse = nodeAResponses.get(0);
        assertRecoveryState(nodeAShardResponse.recoveryState(), 0, Type.GATEWAY, Stage.DONE, nodeA, nodeA, false);
        validateIndexRecoveryState(nodeAShardResponse.recoveryState().getIndex());

        // validate node B recovery
        ShardRecoveryResponse nodeBShardResponse = nodeBResponses.get(0);
        assertRecoveryState(nodeBShardResponse.recoveryState(), 0, Type.REPLICA, Stage.DONE, nodeA, nodeB, false);
        validateIndexRecoveryState(nodeBShardResponse.recoveryState().getIndex());
    }

    @Test
    @TestLogging("indices.recovery:TRACE")
    public void rerouteRecoveryTest() throws Exception {
        logger.info("--> start node A");
        String nodeA = internalCluster().startNode(settingsBuilder().put("gateway.type", "local"));

        logger.info("--> create index on node: {}", nodeA);
        createAndPopulateIndex(INDEX_NAME, 1, SHARD_COUNT, REPLICA_COUNT);

        logger.info("--> start node B");
        String nodeB = internalCluster().startNode(settingsBuilder().put("gateway.type", "local"));

        ensureGreen();

        logger.info("--> slowing down recoveries");
        slowDownRecovery();

        logger.info("--> move shard from: {} to: {}", nodeA, nodeB);
        client().admin().cluster().prepareReroute()
                .add(new MoveAllocationCommand(new ShardId(INDEX_NAME, 0), nodeA, nodeB))
                .execute().actionGet().getState();


        logger.info("--> request recoveries");
        RecoveryResponse response = client().admin().indices().prepareRecoveries(INDEX_NAME).execute().actionGet();

        List<ShardRecoveryResponse> shardResponses = response.shardResponses().get(INDEX_NAME);
        List<ShardRecoveryResponse> nodeAResponses = findRecoveriesForTargetNode(nodeA, shardResponses);
        assertThat(nodeAResponses.size(), equalTo(1));
        List<ShardRecoveryResponse> nodeBResponses = findRecoveriesForTargetNode(nodeB, shardResponses);
        assertThat(nodeBResponses.size(), equalTo(1));

        assertRecoveryState(nodeAResponses.get(0).recoveryState(), 0, Type.GATEWAY, Stage.DONE, nodeA, nodeA, false);
        validateIndexRecoveryState(nodeAResponses.get(0).recoveryState().getIndex());

        assertOnGoingRecoveryState(nodeBResponses.get(0).recoveryState(), 0, Type.RELOCATION, nodeA, nodeB, false);
        validateIndexRecoveryState(nodeBResponses.get(0).recoveryState().getIndex());

        logger.info("--> speeding up recoveries");
        restoreRecoverySpeed();

        // wait for it to be finished
        ensureGreen();

        response = client().admin().indices().prepareRecoveries(INDEX_NAME).execute().actionGet();

        shardResponses = response.shardResponses().get(INDEX_NAME);
        assertThat(shardResponses.size(), equalTo(1));

        assertRecoveryState(shardResponses.get(0).recoveryState(), 0, Type.RELOCATION, Stage.DONE, nodeA, nodeB, false);
        validateIndexRecoveryState(shardResponses.get(0).recoveryState().getIndex());

        logger.info("--> bump replica count");
        client().admin().indices().prepareUpdateSettings(INDEX_NAME)
                .setSettings(settingsBuilder().put("number_of_replicas", 1)).execute().actionGet();
        ensureGreen();

        logger.info("--> start node C");
        String nodeC = internalCluster().startNode(settingsBuilder().put("gateway.type", "local"));
        assertFalse(client().admin().cluster().prepareHealth().setWaitForNodes("3").get().isTimedOut());

        logger.info("--> slowing down recoveries");
        slowDownRecovery();

        logger.info("--> move replica shard from: {} to: {}", nodeA, nodeC);
        client().admin().cluster().prepareReroute()
                .add(new MoveAllocationCommand(new ShardId(INDEX_NAME, 0), nodeA, nodeC))
                .execute().actionGet().getState();

        response = client().admin().indices().prepareRecoveries(INDEX_NAME).execute().actionGet();
        shardResponses = response.shardResponses().get(INDEX_NAME);

        nodeAResponses = findRecoveriesForTargetNode(nodeA, shardResponses);
        assertThat(nodeAResponses.size(), equalTo(1));
        nodeBResponses = findRecoveriesForTargetNode(nodeB, shardResponses);
        assertThat(nodeBResponses.size(), equalTo(1));
        List<ShardRecoveryResponse> nodeCResponses = findRecoveriesForTargetNode(nodeC, shardResponses);
        assertThat(nodeCResponses.size(), equalTo(1));

        assertRecoveryState(nodeAResponses.get(0).recoveryState(), 0, Type.REPLICA, Stage.DONE, nodeB, nodeA, false);
        validateIndexRecoveryState(nodeAResponses.get(0).recoveryState().getIndex());

        assertRecoveryState(nodeBResponses.get(0).recoveryState(), 0, Type.RELOCATION, Stage.DONE, nodeA, nodeB, false);
        validateIndexRecoveryState(nodeBResponses.get(0).recoveryState().getIndex());

        // relocations of replicas are marked as REPLICA and the source node is the node holding the primary (B)
        assertOnGoingRecoveryState(nodeCResponses.get(0).recoveryState(), 0, Type.REPLICA, nodeB, nodeC, false);
        validateIndexRecoveryState(nodeCResponses.get(0).recoveryState().getIndex());

        logger.info("--> speeding up recoveries");
        restoreRecoverySpeed();
        ensureGreen();

        response = client().admin().indices().prepareRecoveries(INDEX_NAME).execute().actionGet();
        shardResponses = response.shardResponses().get(INDEX_NAME);

        nodeAResponses = findRecoveriesForTargetNode(nodeA, shardResponses);
        assertThat(nodeAResponses.size(), equalTo(0));
        nodeBResponses = findRecoveriesForTargetNode(nodeB, shardResponses);
        assertThat(nodeBResponses.size(), equalTo(1));
        nodeCResponses = findRecoveriesForTargetNode(nodeC, shardResponses);
        assertThat(nodeCResponses.size(), equalTo(1));

        assertRecoveryState(nodeBResponses.get(0).recoveryState(), 0, Type.RELOCATION, Stage.DONE, nodeA, nodeB, false);
        validateIndexRecoveryState(nodeBResponses.get(0).recoveryState().getIndex());

        // relocations of replicas are marked as REPLICA and the source node is the node holding the primary (B)
        assertRecoveryState(nodeCResponses.get(0).recoveryState(), 0, Type.REPLICA, Stage.DONE, nodeB, nodeC, false);
        validateIndexRecoveryState(nodeCResponses.get(0).recoveryState().getIndex());
    }

    @Test
    public void snapshotRecoveryTest() throws Exception {
        logger.info("--> start node A");
        String nodeA = internalCluster().startNode(settingsBuilder().put("gateway.type", "local"));

        logger.info("--> create repository");
        assertAcked(client().admin().cluster().preparePutRepository(REPO_NAME)
                .setType("fs").setSettings(ImmutableSettings.settingsBuilder()
                                .put("location", newTempDir(LifecycleScope.SUITE))
                                .put("compress", false)
                ).get());

        ensureGreen();

        logger.info("--> create index on node: {}", nodeA);
        createAndPopulateIndex(INDEX_NAME, 1, SHARD_COUNT, REPLICA_COUNT);

        logger.info("--> snapshot");
        CreateSnapshotResponse createSnapshotResponse = client().admin().cluster().prepareCreateSnapshot(REPO_NAME, SNAP_NAME)
                .setWaitForCompletion(true).setIndices(INDEX_NAME).get();
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), greaterThan(0));
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), equalTo(createSnapshotResponse.getSnapshotInfo().totalShards()));

        assertThat(client().admin().cluster().prepareGetSnapshots(REPO_NAME).setSnapshots(SNAP_NAME).get()
                .getSnapshots().get(0).state(), equalTo(SnapshotState.SUCCESS));

        client().admin().indices().prepareClose(INDEX_NAME).execute().actionGet();

        logger.info("--> restore");
        RestoreSnapshotResponse restoreSnapshotResponse = client().admin().cluster()
                .prepareRestoreSnapshot(REPO_NAME, SNAP_NAME).setWaitForCompletion(true).execute().actionGet();
        int totalShards = restoreSnapshotResponse.getRestoreInfo().totalShards();
        assertThat(totalShards, greaterThan(0));

        ensureGreen();

        logger.info("--> request recoveries");
        RecoveryResponse response = client().admin().indices().prepareRecoveries(INDEX_NAME).execute().actionGet();

        for (Map.Entry<String, List<ShardRecoveryResponse>> shardRecoveryResponse : response.shardResponses().entrySet()) {

            assertThat(shardRecoveryResponse.getKey(), equalTo(INDEX_NAME));
            List<ShardRecoveryResponse> shardRecoveryResponses = shardRecoveryResponse.getValue();
            assertThat(shardRecoveryResponses.size(), equalTo(totalShards));

            for (ShardRecoveryResponse shardResponse : shardRecoveryResponses) {
                assertRecoveryState(shardResponse.recoveryState(), 0, Type.SNAPSHOT, Stage.DONE, null, nodeA, true);
                validateIndexRecoveryState(shardResponse.recoveryState().getIndex());
            }
        }
    }

    private List<ShardRecoveryResponse> findRecoveriesForTargetNode(String nodeName, List<ShardRecoveryResponse> responses) {
        List<ShardRecoveryResponse> nodeResponses = new ArrayList<>();
        for (ShardRecoveryResponse response : responses) {
            if (response.recoveryState().getTargetNode().getName().equals(nodeName)) {
                nodeResponses.add(response);
            }
        }
        return nodeResponses;
    }

    private IndicesStatsResponse createAndPopulateIndex(String name, int nodeCount, int shardCount, int replicaCount)
            throws ExecutionException, InterruptedException {

        logger.info("--> creating test index: {}", name);
        assertAcked(prepareCreate(name, nodeCount, settingsBuilder().put("number_of_shards", shardCount)
                .put("number_of_replicas", replicaCount)));
        ensureGreen();

        logger.info("--> indexing sample data");
        final int numDocs = between(MIN_DOC_COUNT, MAX_DOC_COUNT);
        final IndexRequestBuilder[] docs = new IndexRequestBuilder[numDocs];

        for (int i = 0; i < numDocs; i++) {
            docs[i] = client().prepareIndex(INDEX_NAME, INDEX_TYPE).
                    setSource("foo-int", randomInt(),
                            "foo-string", randomAsciiOfLength(32),
                            "foo-float", randomFloat());
        }

        indexRandom(true, docs);
        flush();
        assertThat(client().prepareCount(INDEX_NAME).get().getCount(), equalTo((long) numDocs));
        return client().admin().indices().prepareStats(INDEX_NAME).execute().actionGet();
    }

    private void validateIndexRecoveryState(RecoveryState.Index indexState) {
        assertThat(indexState.time(), greaterThanOrEqualTo(0L));
        assertThat(indexState.percentFilesRecovered(), greaterThanOrEqualTo(0.0f));
        assertThat(indexState.percentFilesRecovered(), lessThanOrEqualTo(100.0f));
        assertThat(indexState.percentBytesRecovered(), greaterThanOrEqualTo(0.0f));
        assertThat(indexState.percentBytesRecovered(), lessThanOrEqualTo(100.0f));
    }
}