/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.elasticsearch.cluster.routing.allocation;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.RoutingNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.test.ElasticsearchTestCase;
import org.junit.Test;

import static org.elasticsearch.cluster.routing.ShardRoutingState.INITIALIZING;
import static org.elasticsearch.cluster.routing.ShardRoutingState.STARTED;
import static org.elasticsearch.cluster.routing.allocation.RoutingAllocationTests.newNode;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.hamcrest.Matchers.equalTo;

/**
 *
 */
public class RoutingNodesIntegrityTests extends ElasticsearchTestCase {

    private final ESLogger logger = Loggers.getLogger(IndexBalanceTests.class);

    @Test
    public void testBalanceAllNodesStarted() {
        AllocationService strategy = new AllocationService(settingsBuilder()
                .put("cluster.routing.allocation.node_concurrent_recoveries", 10)
                .put("cluster.routing.allocation.node_initial_primaries_recoveries", 10)
                .put("cluster.routing.allocation.allow_rebalance", "always")
                .put("cluster.routing.allocation.cluster_concurrent_rebalance", -1).build());

        logger.info("Building initial routing table");

        MetaData metaData = MetaData.builder().put(IndexMetaData.builder("test").numberOfShards(3).numberOfReplicas(1))
                .put(IndexMetaData.builder("test1").numberOfShards(3).numberOfReplicas(1)).build();

        RoutingTable routingTable = RoutingTable.builder().addAsNew(metaData.index("test")).addAsNew(metaData.index("test1")).build();

        ClusterState clusterState = ClusterState.builder().metaData(metaData).routingTable(routingTable).build();
        RoutingNodes routingNodes = clusterState.routingNodes();

        logger.info("Adding three node and performing rerouting");
        clusterState = ClusterState.builder(clusterState)
                .nodes(DiscoveryNodes.builder().put(newNode("node1")).put(newNode("node2")).put(newNode("node3"))).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        // all shards are unassigned. so no inactive shards or primaries.
        assertThat(routingNodes.hasInactiveShards(), equalTo(false));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(true));

        RoutingTable prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(true));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(true));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

        logger.info("Another round of rebalancing");
        clusterState = ClusterState.builder(clusterState).nodes(DiscoveryNodes.builder(clusterState.nodes())).build();
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        logger.info("Reroute, nothing should change");
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();

        logger.info("Start the more shards");
        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(false));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

    }

    @Test
    public void testBalanceIncrementallyStartNodes() {
        AllocationService strategy = new AllocationService(settingsBuilder()
                .put("cluster.routing.allocation.node_concurrent_recoveries", 10)
                .put("cluster.routing.allocation.node_initial_primaries_recoveries", 10)
                .put("cluster.routing.allocation.allow_rebalance", "always")
                .put("cluster.routing.allocation.cluster_concurrent_rebalance", -1).build());

        logger.info("Building initial routing table");

        MetaData metaData = MetaData.builder().put(IndexMetaData.builder("test").numberOfShards(3).numberOfReplicas(1))
                .put(IndexMetaData.builder("test1").numberOfShards(3).numberOfReplicas(1)).build();

        RoutingTable routingTable = RoutingTable.builder().addAsNew(metaData.index("test")).addAsNew(metaData.index("test1")).build();

        ClusterState clusterState = ClusterState.builder().metaData(metaData).routingTable(routingTable).build();

        logger.info("Adding one node and performing rerouting");
        clusterState = ClusterState.builder(clusterState).nodes(DiscoveryNodes.builder().put(newNode("node1"))).build();

        RoutingTable prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        logger.info("Add another node and perform rerouting, nothing will happen since primary not started");
        clusterState = ClusterState.builder(clusterState)
                .nodes(DiscoveryNodes.builder(clusterState.nodes()).put(newNode("node2"))).build();
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        logger.info("Start the primary shard");
        RoutingNodes routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        logger.info("Reroute, nothing should change");
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();

        logger.info("Start the backup shard");
        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        logger.info("Add another node and perform rerouting, nothing will happen since primary not started");
        clusterState = ClusterState.builder(clusterState)
                .nodes(DiscoveryNodes.builder(clusterState.nodes()).put(newNode("node3"))).build();
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        logger.info("Reroute, nothing should change");
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();

        logger.info("Start the backup shard");
        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(prevRoutingTable != routingTable, equalTo(true));
        assertThat(routingTable.index("test").shards().size(), equalTo(3));

        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(prevRoutingTable != routingTable, equalTo(true));
        assertThat(routingTable.index("test1").shards().size(), equalTo(3));

        assertThat(routingNodes.node("node1").numberOfShardsWithState(STARTED), equalTo(4));
        assertThat(routingNodes.node("node2").numberOfShardsWithState(STARTED), equalTo(4));
        assertThat(routingNodes.node("node3").numberOfShardsWithState(STARTED), equalTo(4));

        assertThat(routingNodes.node("node1").shardsWithState("test", STARTED).size(), equalTo(2));
        assertThat(routingNodes.node("node2").shardsWithState("test", STARTED).size(), equalTo(2));
        assertThat(routingNodes.node("node3").shardsWithState("test", STARTED).size(), equalTo(2));

        assertThat(routingNodes.node("node1").shardsWithState("test1", STARTED).size(), equalTo(2));
        assertThat(routingNodes.node("node2").shardsWithState("test1", STARTED).size(), equalTo(2));
        assertThat(routingNodes.node("node3").shardsWithState("test1", STARTED).size(), equalTo(2));
    }

    @Test
    public void testBalanceAllNodesStartedAddIndex() {
        AllocationService strategy = new AllocationService(settingsBuilder()
                .put("cluster.routing.allocation.node_concurrent_recoveries", 1)
                .put("cluster.routing.allocation.node_initial_primaries_recoveries", 3)
                .put("cluster.routing.allocation.allow_rebalance", "always")
                .put("cluster.routing.allocation.cluster_concurrent_rebalance", -1).build());

        logger.info("Building initial routing table");

        MetaData metaData = MetaData.builder().put(IndexMetaData.builder("test").numberOfShards(3).numberOfReplicas(1)).build();

        RoutingTable routingTable = RoutingTable.builder().addAsNew(metaData.index("test")).build();

        ClusterState clusterState = ClusterState.builder().metaData(metaData).routingTable(routingTable).build();

        logger.info("Adding three node and performing rerouting");
        clusterState = ClusterState.builder(clusterState)
                .nodes(DiscoveryNodes.builder().put(newNode("node1")).put(newNode("node2")).put(newNode("node3"))).build();

        RoutingNodes routingNodes = clusterState.routingNodes();
        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(false));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(true));

        RoutingTable prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(true));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(true));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

        logger.info("Another round of rebalancing");
        clusterState = ClusterState.builder(clusterState).nodes(DiscoveryNodes.builder(clusterState.nodes())).build();
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        assertThat(prevRoutingTable == routingTable, equalTo(true));

        routingNodes = clusterState.routingNodes();
        assertThat(routingNodes.node("node1").numberOfShardsWithState(INITIALIZING), equalTo(1));
        assertThat(routingNodes.node("node2").numberOfShardsWithState(INITIALIZING), equalTo(1));
        assertThat(routingNodes.node("node3").numberOfShardsWithState(INITIALIZING), equalTo(1));

        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(true));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));
        assertThat(routingNodes.node("node1").numberOfShardsWithState(STARTED), equalTo(1));
        assertThat(routingNodes.node("node2").numberOfShardsWithState(STARTED), equalTo(1));
        assertThat(routingNodes.node("node3").numberOfShardsWithState(STARTED), equalTo(1));

        logger.info("Reroute, nothing should change");
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        assertThat(prevRoutingTable == routingTable, equalTo(true));

        logger.info("Start the more shards");
        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(false));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

        assertThat(routingNodes.node("node1").numberOfShardsWithState(STARTED), equalTo(2));
        assertThat(routingNodes.node("node2").numberOfShardsWithState(STARTED), equalTo(2));
        assertThat(routingNodes.node("node3").numberOfShardsWithState(STARTED), equalTo(2));

        assertThat(routingNodes.node("node1").shardsWithState("test", STARTED).size(), equalTo(2));
        assertThat(routingNodes.node("node2").shardsWithState("test", STARTED).size(), equalTo(2));
        assertThat(routingNodes.node("node3").shardsWithState("test", STARTED).size(), equalTo(2));

        logger.info("Add new index 3 shards 1 replica");

        prevRoutingTable = routingTable;
        metaData = MetaData.builder(metaData)
                .put(IndexMetaData.builder("test1").settings(ImmutableSettings.settingsBuilder()
                        .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 3)
                        .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 1)
                ))
                .build();
        routingTable = RoutingTable.builder(routingTable)
                .addAsNew(metaData.index("test1"))
                .build();
        clusterState = ClusterState.builder(clusterState).metaData(metaData).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(false));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(true));

        assertThat(routingTable.index("test1").shards().size(), equalTo(3));

        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        logger.info("Reroute, assign");
        clusterState = ClusterState.builder(clusterState).nodes(DiscoveryNodes.builder(clusterState.nodes())).build();
        prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(true));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(true));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

        assertThat(prevRoutingTable == routingTable, equalTo(true));

        logger.info("Reroute, start the primaries");
        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(true));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

        logger.info("Reroute, start the replicas");
        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(false));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));


        assertThat(routingNodes.node("node1").numberOfShardsWithState(STARTED), equalTo(4));
        assertThat(routingNodes.node("node2").numberOfShardsWithState(STARTED), equalTo(4));
        assertThat(routingNodes.node("node3").numberOfShardsWithState(STARTED), equalTo(4));

        assertThat(routingNodes.node("node1").shardsWithState("test1", STARTED).size(), equalTo(2));
        assertThat(routingNodes.node("node2").shardsWithState("test1", STARTED).size(), equalTo(2));
        assertThat(routingNodes.node("node3").shardsWithState("test1", STARTED).size(), equalTo(2));

        logger.info("kill one node");
        IndexShardRoutingTable indexShardRoutingTable = routingTable.index("test").shard(0);
        clusterState = ClusterState.builder(clusterState).nodes(DiscoveryNodes.builder(clusterState.nodes()).remove(indexShardRoutingTable.primaryShard().currentNodeId())).build();
        routingTable = strategy.reroute(clusterState).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(true));
        // replica got promoted to primary
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

        logger.info("Start Recovering shards round 1");
        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(true));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

        logger.info("Start Recovering shards round 2");
        routingNodes = clusterState.routingNodes();
        prevRoutingTable = routingTable;
        routingTable = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING)).routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();
        routingNodes = clusterState.routingNodes();

        assertThat(assertShardStats(routingNodes), equalTo(true));
        assertThat(routingNodes.hasInactiveShards(), equalTo(false));
        assertThat(routingNodes.hasInactivePrimaries(), equalTo(false));
        assertThat(routingNodes.hasUnassignedPrimaries(), equalTo(false));

    }

    private boolean assertShardStats(RoutingNodes routingNodes) {
        return RoutingNodes.assertShardStats(routingNodes);
    }
}