package com.thinkaurelius.titan.graphdb;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.attribute.*;
import com.thinkaurelius.titan.core.log.*;
import com.thinkaurelius.titan.core.schema.*;
import com.thinkaurelius.titan.core.util.ManagementUtil;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.configuration.ConfigElement;
import com.thinkaurelius.titan.diskstorage.configuration.ConfigOption;
import com.thinkaurelius.titan.diskstorage.configuration.WriteConfiguration;
import com.thinkaurelius.titan.diskstorage.log.Log;
import com.thinkaurelius.titan.diskstorage.log.Message;
import com.thinkaurelius.titan.diskstorage.log.MessageReader;
import com.thinkaurelius.titan.diskstorage.log.ReadMarker;
import com.thinkaurelius.titan.diskstorage.log.kcvs.KCVSLog;
import com.thinkaurelius.titan.diskstorage.util.time.StandardDuration;
import com.thinkaurelius.titan.diskstorage.util.time.Timepoint;
import com.thinkaurelius.titan.diskstorage.util.time.TimestampProvider;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import com.thinkaurelius.titan.graphdb.database.EdgeSerializer;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.database.log.LogTxMeta;
import com.thinkaurelius.titan.graphdb.database.log.LogTxStatus;
import com.thinkaurelius.titan.graphdb.database.log.TransactionLogHeader;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;
import com.thinkaurelius.titan.graphdb.database.serialize.Serializer;
import com.thinkaurelius.titan.graphdb.internal.*;
import com.thinkaurelius.titan.graphdb.log.StandardTransactionLogProcessor;
import com.thinkaurelius.titan.graphdb.olap.job.IndexRemoveJob;
import com.thinkaurelius.titan.graphdb.query.StandardQueryDescription;
import com.thinkaurelius.titan.graphdb.query.vertex.BasicVertexCentricQueryBuilder;
import com.thinkaurelius.titan.graphdb.relations.RelationIdentifier;
import com.thinkaurelius.titan.graphdb.schema.EdgeLabelDefinition;
import com.thinkaurelius.titan.graphdb.schema.PropertyKeyDefinition;
import com.thinkaurelius.titan.graphdb.schema.SchemaContainer;
import com.thinkaurelius.titan.graphdb.schema.VertexLabelDefinition;
import com.thinkaurelius.titan.graphdb.serializer.SpecialInt;
import com.thinkaurelius.titan.graphdb.serializer.SpecialIntSerializer;
import com.thinkaurelius.titan.graphdb.tinkerpop.optimize.TitanGraphStep;
import com.thinkaurelius.titan.graphdb.tinkerpop.optimize.TitanVertexStep;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.graphdb.types.StandardEdgeLabelMaker;
import com.thinkaurelius.titan.graphdb.types.StandardPropertyKeyMaker;
import com.thinkaurelius.titan.graphdb.types.system.BaseVertexLabel;
import com.thinkaurelius.titan.graphdb.types.system.ImplicitKey;
import com.thinkaurelius.titan.testcategory.BrittleTests;
import com.thinkaurelius.titan.testutil.TestGraphConfigs;

import org.apache.tinkerpop.gremlin.structure.Direction;
import static org.apache.tinkerpop.gremlin.structure.Direction.*;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration.*;
import static com.thinkaurelius.titan.graphdb.internal.RelationCategory.*;
import static com.thinkaurelius.titan.testutil.TitanAssert.*;
import static org.apache.tinkerpop.gremlin.structure.Order.*;
import static org.junit.Assert.*;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class TitanGraphTest extends TitanGraphBaseTest {

    private Logger log = LoggerFactory.getLogger(TitanGraphTest.class);

    protected abstract boolean isLockingOptimistic();

  /* ==================================================================================
                            INDEXING
     ==================================================================================*/

    /**
     * Just opens and closes the graph
     */
    @Test
    public void testOpenClose() {
    }

    /**
     * Very simple graph operation to ensure minimal functionality and cleanup
     */
    @Test
    public void testBasic() {

        PropertyKey uid = makeVertexIndexedUniqueKey("name",String.class);
        finishSchema();

        TitanVertex n1 = tx.addVertex();
        uid = tx.getPropertyKey("name");
        n1.property(uid.name(), "abcd");
        clopen();
        long nid = n1.longId();
        uid = tx.getPropertyKey("name");
        assertTrue(getV(tx,nid) != null);
        assertTrue(getV(tx,uid.longId())!=null);
        assertMissing(tx,nid+64);
        uid = tx.getPropertyKey(uid.name());
        n1 = getV(tx,nid);
        assertEquals(n1,getOnlyVertex(tx.query().has(uid.name(),"abcd")));
        assertEquals(1, Iterables.size(n1.query().relations())); //TODO: how to expose relations?
        assertEquals("abcd",n1.value(uid.name()));
        assertCount(1,tx.query().vertices());
        close();
        TitanCleanup.clear(graph);
        open(config);
        assertEmpty(tx.query().vertices());
    }

    /**
     * Adding a removing a vertex with index
     */
    @Test
    public void testVertexRemoval() {
        final String namen = "name";
        makeVertexIndexedUniqueKey(namen,String.class);
        finishSchema();

        TitanVertex v1 = graph.addVertex(namen,"v1");
        TitanVertex v2 = graph.addVertex(namen,"v2");
        v1.addEdge("knows",v2);
        assertCount(2,graph.query().vertices());
        assertCount(1,graph.query().has(namen,"v2").vertices());

        clopen();

        v1 = getV(graph, v1);
        v2 = getV(graph, v2);
        assertCount(1,v1.query().direction(BOTH).edges());
        assertCount(1,v2.query().direction(Direction.BOTH).edges());
        v2.remove();
        assertCount(0,v1.query().direction(Direction.BOTH).edges());
        try {
            assertCount(0,v2.query().direction(Direction.BOTH).edges());
            fail();
        } catch (IllegalStateException ex) {}
        assertCount(1,graph.query().vertices());
        assertCount(1,graph.query().has(namen,"v1").vertices());
        assertCount(0,graph.query().has(namen,"v2").vertices());
        graph.tx().commit();

        assertMissing(graph,v2);
        assertCount(1,graph.query().vertices());
        assertCount(1,graph.query().has(namen,"v1").vertices());
        assertCount(0,graph.query().has(namen,"v2").vertices());
    }

    /**
     * Iterating over all vertices and edges in a graph
     */
    @Test
    public void testGlobalIteration() {
        int numV = 50;
        int deleteV = 5;

        TitanVertex previous = tx.addVertex("count",0);
        for (int i = 1; i < numV; i++) {
            TitanVertex next = tx.addVertex("count",i);
            previous.addEdge("next", next);
            previous = next;
        }
        int numE = numV - 1;
        assertCount(numV, tx.query().vertices());
        assertCount(numV, tx.query().vertices());
        assertCount(numE, tx.query().edges());
        assertCount(numE, tx.query().edges());

        clopen();

        assertCount(numV, tx.query().vertices());
        assertCount(numV, tx.query().vertices());
        assertCount(numE, tx.query().edges());
        assertCount(numE, tx.query().edges());

        //tx.V().range(0,deleteV).remove();
        for (TitanVertex v : tx.query().limit(deleteV).vertices()) {
            v.remove();
        }

        for (int i=0;i<10;i++) { //Repeated vertex counts
            assertCount(numV - deleteV, tx.query().vertices());
            assertCount(numV - deleteV, tx.query().has("count",Cmp.GREATER_THAN_EQUAL,0).vertices());
        }

        clopen();
        for (int i=0;i<10;i++) { //Repeated vertex counts
            assertCount(numV - deleteV, tx.query().vertices());
            assertCount(numV - deleteV, tx.query().has("count",Cmp.GREATER_THAN_EQUAL,0).vertices());
        }
    }

    @Test
    public void testMediumCreateRetrieve() {
        //Create schema
        makeLabel("connect");
        makeVertexIndexedUniqueKey("name",String.class);
        PropertyKey weight = makeKey("weight",Double.class);
        PropertyKey id = makeVertexIndexedUniqueKey("uid",Integer.class);
        ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("knows")).sortKey(id).signature(weight).make();
        finishSchema();

        //Create Nodes
        int noVertices = 500;
        String[] names = new String[noVertices];
        int[] ids = new int[noVertices];
        TitanVertex[] nodes = new TitanVertex[noVertices];
        long[] nodeIds = new long[noVertices];
        List[] nodeEdges = new List[noVertices];
        for (int i = 0; i < noVertices; i++) {
            names[i] = "vertex" + i;
            ids[i] = i;
            nodes[i] = tx.addVertex("name",names[i],"uid",ids[i]);
            if ((i + 1) % 100 == 0) log.debug("Added 100 nodes");
        }
        log.debug("Nodes created");
        int[] connectOff = {-100, -34, -4, 10, 20};
        int[] knowsOff = {-400, -18, 8, 232, 334};
        for (int i = 0; i < noVertices; i++) {
            TitanVertex n = nodes[i];
            nodeEdges[i] = new ArrayList(10);
            for (int c : connectOff) {
                Edge r = n.addEdge("connect", nodes[wrapAround(i + c, noVertices)]);
                nodeEdges[i].add(r);
            }
            for (int k : knowsOff) {
                TitanVertex n2 = nodes[wrapAround(i + k, noVertices)];
                Edge r = n.addEdge("knows", n2,
                        "uid", ((Number) n.value("uid")).intValue() + ((Number) n2.value("uid")).intValue(),
                        "weight", k * 1.5,
                        "name", i + "-" + k);
                nodeEdges[i].add(r);
            }
            if (i % 100 == 99) log.debug(".");
        }

        tx.commit();
        tx = null;
        Set[] nodeEdgeIds = new Set[noVertices];
        for (int i = 0; i < noVertices; i++) {
            nodeIds[i] = (Long)nodes[i].id();
            nodeEdgeIds[i] = new HashSet(10);
            for (Object r : nodeEdges[i]) {
                nodeEdgeIds[i].add(((TitanEdge) r).longId());
            }
        }
        clopen();

        nodes = new TitanVertex[noVertices];
        for (int i = 0; i < noVertices; i++) {
            TitanVertex n = getVertex("uid", ids[i]);
            assertEquals(n, getVertex("name", names[i]));
            assertEquals(names[i], n.value("name"));
            nodes[i] = n;
            assertEquals(nodeIds[i], n.longId());
        }
        for (int i = 0; i < noVertices; i++) {
            TitanVertex n = nodes[i];
            assertCount(connectOff.length + knowsOff.length, n.query().direction(Direction.OUT).edges());
            assertCount(connectOff.length, n.query().direction(Direction.OUT).labels("connect").edges());
            assertCount(connectOff.length * 2, n.query().direction(Direction.BOTH).labels("connect").edges());
            assertCount(knowsOff.length * 2,  n.query().direction(Direction.BOTH).labels("knows").edges());

            assertCount(connectOff.length + knowsOff.length, n.query().direction(Direction.OUT).edges());
            assertCount(2, n.properties());
            for (TitanEdge r : n.query().direction(Direction.OUT).labels("knows").edges()) {
                TitanVertex n2 = r.vertex(Direction.IN);
                int idsum = ((Number) n.value("uid")).intValue() + ((Number) n2.value("uid")).intValue();
                assertEquals(idsum, ((Number)r.value("uid")).intValue());
                double k = ((Number) r.value("weight")).doubleValue() / 1.5;
                int ki = (int) k;
                assertEquals(i + "-" + ki, r.value("name"));
            }

            Set edgeIds = new HashSet(10);
            for (TitanEdge r : n.query().direction(Direction.OUT).edges()) {
                edgeIds.add(((TitanEdge)r).longId());
            }
            assertTrue(edgeIds + " vs " + nodeEdgeIds[i], edgeIds.equals(nodeEdgeIds[i]));
        }
        newTx();
        //Bulk vertex retrieval
        long[] vids1 = new long[noVertices/10];
        for (int i = 0; i < vids1.length; i++) {
            vids1[i]=nodeIds[i];
        }
        //All non-cached
        verifyVerticesRetrieval(vids1, Lists.newArrayList(tx.getVertices(vids1)));

        //All cached
        verifyVerticesRetrieval(vids1, Lists.newArrayList(tx.getVertices(vids1)));

        long[] vids2 = new long[noVertices/10*2];
        for (int i = 0; i < vids2.length; i++) {
            vids2[i]=nodeIds[i];
        }
        //Partially cached
        verifyVerticesRetrieval(vids2, Lists.newArrayList(tx.getVertices(vids2)));
    }

    private void verifyVerticesRetrieval(long[] vids, List<TitanVertex> vs) {
        assertEquals(vids.length,vs.size());
        Set<Long> vset = new HashSet<>(vs.size());
        vs.forEach(v -> vset.add((Long)v.id()));
        for (int i = 0; i < vids.length; i++) {
            assertTrue(vset.contains(vids[i]));
        }
    }


    /* ==================================================================================
                            SCHEMA TESTS
     ==================================================================================*/

    /**
     * Test the definition and inspection of various schema types and ensure their correct interpretation
     * within the graph
     */
    @Test
    public void testSchemaTypes() {
        // ---------- PROPERTY KEYS ----------------
        //Normal single-valued property key
        PropertyKey weight = makeKey("weight",Decimal.class);
        //Indexed unique property key
        PropertyKey uid = makeVertexIndexedUniqueKey("uid",String.class);
        //Indexed but not unique
        PropertyKey someid = makeVertexIndexedKey("someid", Object.class);
        //Set-valued property key
        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.SET).make();
        //List-valued property key with signature
        PropertyKey value = mgmt.makePropertyKey("value").dataType(Precision.class).signature(weight).cardinality(Cardinality.LIST).make();

        // ---------- EDGE LABELS ----------------
        //Standard edge label
        EdgeLabel friend = mgmt.makeEdgeLabel("friend").make();
        //Unidirected
        EdgeLabel link = mgmt.makeEdgeLabel("link").unidirected().multiplicity(Multiplicity.MANY2ONE).make();
        //Signature label
        EdgeLabel connect = mgmt.makeEdgeLabel("connect").signature(uid, link).multiplicity(Multiplicity.SIMPLE).make();
        //Edge labels with different cardinalities
        EdgeLabel parent = mgmt.makeEdgeLabel("parent").multiplicity(Multiplicity.MANY2ONE).make();
        EdgeLabel child = mgmt.makeEdgeLabel("child").multiplicity(Multiplicity.ONE2MANY).make();
        EdgeLabel spouse = mgmt.makeEdgeLabel("spouse").multiplicity(Multiplicity.ONE2ONE).make();

        // ---------- VERTEX LABELS ----------------

        VertexLabel person = mgmt.makeVertexLabel("person").make();
        VertexLabel tag = mgmt.makeVertexLabel("tag").make();
        VertexLabel tweet = mgmt.makeVertexLabel("tweet").setStatic().make();

        long[] sig;

        // ######### INSPECTION & FAILURE ############

        assertTrue(mgmt.isOpen());
        assertEquals("weight",weight.toString());
        assertTrue(mgmt.containsRelationType("weight"));
        assertTrue(mgmt.containsPropertyKey("weight"));
        assertFalse(mgmt.containsEdgeLabel("weight"));
        assertTrue(mgmt.containsEdgeLabel("connect"));
        assertFalse(mgmt.containsPropertyKey("connect"));
        assertFalse(mgmt.containsRelationType("bla"));
        assertNull(mgmt.getPropertyKey("bla"));
        assertNull(mgmt.getEdgeLabel("bla"));
        assertNotNull(mgmt.getPropertyKey("weight"));
        assertNotNull(mgmt.getEdgeLabel("connect"));
        assertTrue(weight.isPropertyKey());
        assertFalse(weight.isEdgeLabel());
        assertEquals(Cardinality.SINGLE,weight.cardinality());
        assertEquals(Cardinality.SINGLE,someid.cardinality());
        assertEquals(Cardinality.SET,name.cardinality());
        assertEquals(Cardinality.LIST,value.cardinality());
        assertEquals(Object.class,someid.dataType());
        assertEquals(Decimal.class,weight.dataType());
        sig = ((InternalRelationType)value).getSignature();
        assertEquals(1,sig.length);
        assertEquals(weight.longId(),sig[0]);
        assertTrue(mgmt.getGraphIndex(uid.name()).isUnique());
        assertFalse(mgmt.getGraphIndex(someid.name()).isUnique());

        assertEquals("friend", friend.name());
        assertTrue(friend.isEdgeLabel());
        assertFalse(friend.isPropertyKey());
        assertEquals(Multiplicity.ONE2ONE, spouse.multiplicity());
        assertEquals(Multiplicity.ONE2MANY,child.multiplicity());
        assertEquals(Multiplicity.MANY2ONE,parent.multiplicity());
        assertEquals(Multiplicity.MULTI,friend.multiplicity());
        assertEquals(Multiplicity.SIMPLE, connect.multiplicity());
        assertTrue(link.isUnidirected());
        assertFalse(link.isDirected());
        assertFalse(child.isUnidirected());
        assertTrue(spouse.isDirected());
        assertFalse(((InternalRelationType) friend).isInvisibleType());
        assertTrue(((InternalRelationType) friend).isInvisible());
        assertEquals(0, ((InternalRelationType) friend).getSignature().length);
        sig = ((InternalRelationType)connect).getSignature();
        assertEquals(2,sig.length);
        assertEquals(uid.longId(),sig[0]);
        assertEquals(link.longId(),sig[1]);
        assertEquals(0,((InternalRelationType) friend).getSortKey().length);
        assertEquals(Order.DEFAULT,((InternalRelationType) friend).getSortOrder());
        assertEquals(SchemaStatus.ENABLED,((InternalRelationType)friend).getStatus());

        assertEquals(5,Iterables.size(mgmt.getRelationTypes(PropertyKey.class)));
        assertEquals(6,Iterables.size(mgmt.getRelationTypes(EdgeLabel.class)));
        assertEquals(11,Iterables.size(mgmt.getRelationTypes(RelationType.class)));
        assertEquals(3,Iterables.size(mgmt.getVertexLabels()));

        assertEquals("tweet",tweet.name());
        assertTrue(mgmt.containsVertexLabel("person"));
        assertFalse(mgmt.containsVertexLabel("bla"));
        assertFalse(person.isPartitioned());
        assertFalse(person.isStatic());
        assertFalse(tag.isPartitioned());
        assertTrue(tweet.isStatic());

        //------ TRY INVALID STUFF --------

        //Failures
        try {
            //No datatype
            mgmt.makePropertyKey("fid").make();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Already exists
            mgmt.makeEdgeLabel("link").unidirected().make();
            fail();
        } catch (SchemaViolationException e) {}
        try {
            //signature and sort-key collide
            ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("other")).
                    sortKey(someid, weight).signature(someid).make();
            fail();
        } catch (IllegalArgumentException e) {}
//        try {
//            //keys must be single-valued
//            ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("other")).
//                    sortKey(name, weight).make();
//            fail();
//        } catch (IllegalArgumentException e) {}
        try {
            //sort key requires the label to be non-constrained
            ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("other")).multiplicity(Multiplicity.SIMPLE).
                    sortKey(weight).make();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //sort key requires the label to be non-constrained
            ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("other")).multiplicity(Multiplicity.MANY2ONE).
                    sortKey(weight).make();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Already exists
            mgmt.makeVertexLabel("tweet").make();
            fail();
        } catch (SchemaViolationException e) {}
        try {
            //only unidrected, Many2One labels are allowed in signatures
            mgmt.makeEdgeLabel("test").signature(friend).make();
            fail();
        } catch (IllegalArgumentException e) {}

        // ######### END INSPECTION ############


        finishSchema();
        clopen();

        //Load schema types into current transaction
        weight = mgmt.getPropertyKey("weight");
        uid = mgmt.getPropertyKey("uid");
        someid = mgmt.getPropertyKey("someid");
        name = mgmt.getPropertyKey("name");
        value = mgmt.getPropertyKey("value");
        friend = mgmt.getEdgeLabel("friend");
        link = mgmt.getEdgeLabel("link");
        connect = mgmt.getEdgeLabel("connect");
        parent = mgmt.getEdgeLabel("parent");
        child = mgmt.getEdgeLabel("child");
        spouse = mgmt.getEdgeLabel("spouse");
        person = mgmt.getVertexLabel("person");
        tag = mgmt.getVertexLabel("tag");
        tweet = mgmt.getVertexLabel("tweet");


        // ######### INSPECTION & FAILURE (COPIED FROM ABOVE) ############

        assertTrue(mgmt.isOpen());
        assertEquals("weight",weight.toString());
        assertTrue(mgmt.containsRelationType("weight"));
        assertTrue(mgmt.containsPropertyKey("weight"));
        assertFalse(mgmt.containsEdgeLabel("weight"));
        assertTrue(mgmt.containsEdgeLabel("connect"));
        assertFalse(mgmt.containsPropertyKey("connect"));
        assertFalse(mgmt.containsRelationType("bla"));
        assertNull(mgmt.getPropertyKey("bla"));
        assertNull(mgmt.getEdgeLabel("bla"));
        assertNotNull(mgmt.getPropertyKey("weight"));
        assertNotNull(mgmt.getEdgeLabel("connect"));
        assertTrue(weight.isPropertyKey());
        assertFalse(weight.isEdgeLabel());
        assertEquals(Cardinality.SINGLE, weight.cardinality());
        assertEquals(Cardinality.SINGLE, someid.cardinality());
        assertEquals(Cardinality.SET, name.cardinality());
        assertEquals(Cardinality.LIST,value.cardinality());
        assertEquals(Object.class,someid.dataType());
        assertEquals(Decimal.class,weight.dataType());
        sig = ((InternalRelationType)value).getSignature();
        assertEquals(1,sig.length);
        assertEquals(weight.longId(),sig[0]);
        assertTrue(mgmt.getGraphIndex(uid.name()).isUnique());
        assertFalse(mgmt.getGraphIndex(someid.name()).isUnique());

        assertEquals("friend",friend.name());
        assertTrue(friend.isEdgeLabel());
        assertFalse(friend.isPropertyKey());
        assertEquals(Multiplicity.ONE2ONE, spouse.multiplicity());
        assertEquals(Multiplicity.ONE2MANY,child.multiplicity());
        assertEquals(Multiplicity.MANY2ONE, parent.multiplicity());
        assertEquals(Multiplicity.MULTI, friend.multiplicity());
        assertEquals(Multiplicity.SIMPLE, connect.multiplicity());
        assertTrue(link.isUnidirected());
        assertFalse(link.isDirected());
        assertFalse(child.isUnidirected());
        assertTrue(spouse.isDirected());
        assertFalse(((InternalRelationType) friend).isInvisibleType());
        assertTrue(((InternalRelationType) friend).isInvisible());
        assertEquals(0, ((InternalRelationType) friend).getSignature().length);
        sig = ((InternalRelationType)connect).getSignature();
        assertEquals(2, sig.length);
        assertEquals(uid.longId(), sig[0]);
        assertEquals(link.longId(),sig[1]);
        assertEquals(0,((InternalRelationType) friend).getSortKey().length);
        assertEquals(Order.DEFAULT,((InternalRelationType) friend).getSortOrder());
        assertEquals(SchemaStatus.ENABLED,((InternalRelationType)friend).getStatus());

        assertEquals(5,Iterables.size(mgmt.getRelationTypes(PropertyKey.class)));
        assertEquals(6,Iterables.size(mgmt.getRelationTypes(EdgeLabel.class)));
        assertEquals(11,Iterables.size(mgmt.getRelationTypes(RelationType.class)));
        assertEquals(3,Iterables.size(mgmt.getVertexLabels()));

        assertEquals("tweet",tweet.name());
        assertTrue(mgmt.containsVertexLabel("person"));
        assertFalse(mgmt.containsVertexLabel("bla"));
        assertFalse(person.isPartitioned());
        assertFalse(person.isStatic());
        assertFalse(tag.isPartitioned());
        assertTrue(tweet.isStatic());

        //------ TRY INVALID STUFF --------

        //Failures
        try {
            //No datatype
            mgmt.makePropertyKey("fid").make();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Already exists
            mgmt.makeEdgeLabel("link").unidirected().make();
            fail();
        } catch (SchemaViolationException e) {}
        try {
            //signature and sort-key collide
            ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("other")).
                    sortKey(someid, weight).signature(someid).make();
            fail();
        } catch (IllegalArgumentException e) {}
//        try {
//            //keys must be single-valued
//            ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("other")).
//                    sortKey(name, weight).make();
//            fail();
//        } catch (IllegalArgumentException e) {}
        try {
            //sort key requires the label to be non-constrained
            ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("other")).multiplicity(Multiplicity.SIMPLE).
                    sortKey(weight).make();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //sort key requires the label to be non-constrained
            ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("other")).multiplicity(Multiplicity.MANY2ONE).
                    sortKey(weight).make();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Already exists
            mgmt.makeVertexLabel("tweet").make();
            fail();
        } catch (SchemaViolationException e) {}
        try {
            //only unidrected, Many2One labels are allowed in signatures
            mgmt.makeEdgeLabel("test").signature(friend).make();
            fail();
        } catch (IllegalArgumentException e) {}

        // ######### END INSPECTION ############

        /*
          ####### Make sure schema semantics are honored in transactions ######
        */

        clopen();

        TitanTransaction tx2;
        assertEmpty(tx.query().has("uid","v1").vertices()); //shouldn't exist

        TitanVertex v = tx.addVertex();
        //test property keys
        v.property("uid", "v1");
        v.property("weight", 1.5);
        v.property("someid", "Hello");
        v.property("name", "Bob");
        v.property("name", "John");
        VertexProperty p = v.property("value", 11);
        p.property("weight",22);
        v.property("value",33.3,"weight",66.6);
        v.property("value",11,"weight",22); //same values are supported for list-properties
        //test edges
        TitanVertex v12 = tx.addVertex("person"), v13 = tx.addVertex("person");
        v12.property("uid", "v12");
        v13.property("uid", "v13");
        v12.addEdge("parent", v, "weight", 4.5);
        v13.addEdge("parent", v, "weight", 4.5);
        v.addEdge("child", v12);
        v.addEdge("child", v13);
        v.addEdge("spouse", v12);
        v.addEdge("friend",v12);
        v.addEdge("friend",v12); //supports multi edges
        v.addEdge("connect", v12,"uid","e1","link",v);
        v.addEdge("link",v13);
        TitanVertex v2 = tx.addVertex("tweet");
        v2.addEdge("link",v13);
        v12.addEdge("connect",v2);
        TitanEdge edge;

        // ######### INSPECTION & FAILURE ############
        assertEquals(v, (Vertex)getOnlyElement(tx.query().has("uid", Cmp.EQUAL, "v1").vertices()));
        v = getOnlyVertex(tx.query().has("uid",Cmp.EQUAL,"v1"));
        v12 = getOnlyVertex(tx.query().has("uid", Cmp.EQUAL, "v12"));
        v13 = getOnlyVertex(tx.query().has("uid",Cmp.EQUAL,"v13"));
        try {
            //Invalid data type
            v.property("weight", "x");
            fail();
        } catch (SchemaViolationException e) {}
        try {
            //Only one "John" should be allowed
            v.property("name","John");
            fail();
        } catch (SchemaViolationException e) {}


        //Only one property for weight allowed
        v.property(single,"weight", 1.0);
        assertCount(1,v.properties("weight"));
        v.property(VertexProperty.Cardinality.single, "weight", 0.5);
        assertEquals(0.5,v.<Decimal>value("weight").doubleValue(),0.00001);
        assertEquals("v1",v.value("uid"));
        assertCount(2, v.properties("name"));
        for (TitanVertexProperty<String> prop : v.query().labels("name").properties()) {
            String nstr = prop.value();
            assertTrue(nstr.equals("Bob") || nstr.equals("John"));
        }
        assertTrue(size(v.properties("value")) >= 3);
        for (TitanVertexProperty<Precision> prop : v.query().labels("value").properties()) {
            double prec = prop.value().doubleValue();
            assertEquals(prec*2,prop.<Number>value("weight").doubleValue(),0.00001);
        }
        //Ensure we can add additional values
        p = v.property("value",44.4,"weight",88.8);
        assertEquals(v, (Vertex)getOnlyElement(tx.query().has("someid",Cmp.EQUAL,"Hello").vertices()));

        //------- EDGES -------
        try {
            //multiplicity violation
            v12.addEdge("parent", v13);
            fail();
        } catch (SchemaViolationException e) {
        }
        try {
            //multiplicity violation
            v13.addEdge("child", v12);
            fail();
        } catch (SchemaViolationException e) {
        }
        try {
            //multiplicity violation
            v13.addEdge("spouse", v12);
            fail();
        } catch (SchemaViolationException e) {
        }
        try {
            //multiplicity violation
            v.addEdge("spouse", v13);
            fail();
        } catch (SchemaViolationException e) {
        }
        assertCount(2, v.query().direction(Direction.IN).labels("parent").edges());
        assertCount(1, v12.query().direction(Direction.OUT).labels("parent").has("weight").edges());
        assertCount(1, v13.query().direction(Direction.OUT).labels("parent").has("weight").edges());
        assertEquals(v12,getOnlyElement(v.query().direction(Direction.OUT).labels("spouse").vertices()));
        edge = getOnlyElement(v.query().direction(Direction.BOTH).labels("connect").edges());
        assertEquals(2,edge.keys().size());
        assertEquals("e1",edge.value("uid"));
        assertEquals(v,edge.value("link"));
        try {
            //connect is simple
            v.addEdge("connect", v12);
            fail();
        } catch (SchemaViolationException e) {
        }
        //Make sure "link" is unidirected
        assertCount(1, v.query().direction(Direction.BOTH).labels("link").edges());
        assertCount(0, v13.query().direction(Direction.BOTH).labels("link").edges());
        //Assert we can add more friendships
        v.addEdge("friend",v12);
        v2 = getOnlyElement(v12.query().direction(Direction.OUT).labels("connect").vertices());
        assertEquals(v13,getOnlyElement(v2.query().direction(Direction.OUT).labels("link").vertices()));

        assertEquals(BaseVertexLabel.DEFAULT_VERTEXLABEL.name(),v.label());
        assertEquals("person",v12.label());
        assertEquals("person", v13.label());

        assertCount(4, tx.query().vertices());

        // ######### END INSPECTION & FAILURE ############

        clopen();

        // ######### INSPECTION & FAILURE (copied from above) ############
        assertEquals(v, getOnlyVertex(tx.query().has("uid", Cmp.EQUAL, "v1")));
        v = getOnlyVertex(tx.query().has("uid",Cmp.EQUAL,"v1"));
        v12 = getOnlyVertex(tx.query().has("uid", Cmp.EQUAL, "v12"));
        v13 = getOnlyVertex(tx.query().has("uid", Cmp.EQUAL, "v13"));
        try {
            //Invalid data type
            v.property("weight", "x");
            fail();
        } catch (SchemaViolationException e) {}
        try {
            //Only one "John" should be allowed
            v.property("name", "John");
            fail();
        } catch (SchemaViolationException e) {}

        //Only one property for weight allowed
        v.property(VertexProperty.Cardinality.single, "weight",  1.0);
        assertCount(1, v.properties("weight"));
        v.property(VertexProperty.Cardinality.single, "weight",  0.5);
        assertEquals(0.5,v.<Decimal>value("weight").doubleValue(),0.00001);
        assertEquals("v1",v.value("uid"));
        assertCount(2, v.properties("name"));
        for (TitanVertexProperty<String> prop : v.query().labels("name").properties()) {
            String nstr = prop.value();
            assertTrue(nstr.equals("Bob") || nstr.equals("John"));
        }
        assertTrue(Iterables.size(v.query().labels("value").properties()) >= 3);
        for (TitanVertexProperty<Precision> prop : v.query().labels("value").properties()) {
            double prec = prop.value().doubleValue();
            assertEquals(prec*2,prop.<Number>value("weight").doubleValue(),0.00001);
        }
        //Ensure we can add additional values
        p = v.property("value", 44.4, "weight", 88.8);
        assertEquals(v, (Vertex)getOnlyElement(tx.query().has("someid", Cmp.EQUAL, "Hello").vertices()));

        //------- EDGES -------
        try {
            //multiplicity violation
            v12.addEdge("parent", v13);
            fail();
        } catch (SchemaViolationException e) {
        }
        try {
            //multiplicity violation
            v13.addEdge("child", v12);
            fail();
        } catch (SchemaViolationException e) {
        }
        try {
            //multiplicity violation
            v13.addEdge("spouse", v12);
            fail();
        } catch (SchemaViolationException e) {
        }
        try {
            //multiplicity violation
            v.addEdge("spouse", v13);
            fail();
        } catch (SchemaViolationException e) {
        }
        assertCount(2, v.query().direction(Direction.IN).labels("parent").edges());
        assertCount(1, v12.query().direction(Direction.OUT).labels("parent").has("weight").edges());
        assertCount(1, v13.query().direction(Direction.OUT).labels("parent").has("weight").edges());
        assertEquals(v12,getOnlyElement(v.query().direction(Direction.OUT).labels("spouse").vertices()));
        edge = getOnlyElement(v.query().direction(Direction.BOTH).labels("connect").edges());
        assertEquals(2,edge.keys().size());
        assertEquals("e1",edge.value("uid"));
        assertEquals(v,edge.value("link"));
        try {
            //connect is simple
            v.addEdge("connect", v12);
            fail();
        } catch (SchemaViolationException e) {
        }
        //Make sure "link" is unidirected
        assertCount(1, v.query().direction(Direction.BOTH).labels("link").edges());
        assertCount(0, v13.query().direction(Direction.BOTH).labels("link").edges());
        //Assert we can add more friendships
        v.addEdge("friend",v12);
        v2 = getOnlyElement(v12.query().direction(Direction.OUT).labels("connect").vertices());
        assertEquals(v13,getOnlyElement(v2.query().direction(Direction.OUT).labels("link").vertices()));

        assertEquals(BaseVertexLabel.DEFAULT_VERTEXLABEL.name(),v.label());
        assertEquals("person",v12.label());
        assertEquals("person", v13.label());

        assertCount(4, tx.query().vertices());

        // ######### END INSPECTION & FAILURE ############

        //Ensure index uniqueness enforcement
        tx2 = graph.newTransaction();
        try {
            TitanVertex vx = tx2.addVertex();
            try {
                //property is unique
                vx.property(VertexProperty.Cardinality.single, "uid", "v1");
                fail();
            } catch (SchemaViolationException e) {}
            vx.property(VertexProperty.Cardinality.single, "uid",  "unique");
            TitanVertex vx2 = tx2.addVertex();
            try {
                //property unique
                vx2.property(VertexProperty.Cardinality.single, "uid", "unique");
                fail();
            } catch (SchemaViolationException e) {}
        } finally {
            tx2.rollback();
        }


        //Ensure that v2 is really static
        v2 = getV(tx,v2);
        assertEquals("tweet",v2.label());
        try {
            v2.property(VertexProperty.Cardinality.single, "weight", 11);
            fail();
        } catch (SchemaViolationException e) {}
        try {
            v2.addEdge("friend",v12);
            fail();
        } catch (SchemaViolationException e) {}

        //Ensure that unidirected edges keep pointing to deleted vertices
        getV(tx,v13).remove();
        assertCount(1, v.query().direction(Direction.BOTH).labels("link").edges());

        //Finally, test the schema container
        SchemaContainer schemaContainer = new SchemaContainer(graph);
        assertTrue(schemaContainer.containsRelationType("weight"));
        assertTrue(schemaContainer.containsRelationType("friend"));
        assertTrue(schemaContainer.containsVertexLabel("person"));
        VertexLabelDefinition vld = schemaContainer.getVertexLabel("tag");
        assertFalse(vld.isPartitioned());
        assertFalse(vld.isStatic());
        PropertyKeyDefinition pkd = schemaContainer.getPropertyKey("name");
        assertEquals(Cardinality.SET,pkd.getCardinality());
        assertEquals(String.class,pkd.getDataType());
        EdgeLabelDefinition eld = schemaContainer.getEdgeLabel("child");
        assertEquals("child",eld.getName());
        assertEquals(child.longId(),eld.getLongId());
        assertEquals(Multiplicity.ONE2MANY,eld.getMultiplicity());
        assertFalse(eld.isUnidirected());
    }

    /**
     * Test the different data types that Titan supports natively and ensure that invalid data types aren't allowed
     */
    @Test
    public void testDataTypes() throws Exception {
        clopen(option(CUSTOM_ATTRIBUTE_CLASS,"attribute10"),SpecialInt.class.getCanonicalName(),
                option(CUSTOM_SERIALIZER_CLASS,"attribute10"),SpecialIntSerializer.class.getCanonicalName());

        PropertyKey num = makeKey("num",SpecialInt.class);

        PropertyKey barr = makeKey("barr",byte[].class);

        PropertyKey boolval = makeKey("boolval",Boolean.class);

        PropertyKey birthday = makeKey("birthday",GregorianCalendar.class);

        PropertyKey geo = makeKey("geo", Geoshape.class);

        PropertyKey precise = makeKey("precise",Precision.class);

        PropertyKey any = mgmt.makePropertyKey("any").cardinality(Cardinality.LIST).dataType(Object.class).make();

        try {
            //Not a valid data type - primitive
            makeKey("pint",int.class);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            //Not a valid data type - interface
            makeKey("number", Number.class);
            fail();
        } catch (IllegalArgumentException e) {
        }

        finishSchema();
        clopen();

        boolval = tx.getPropertyKey("boolval");
        num = tx.getPropertyKey("num");
        barr = tx.getPropertyKey("barr");
        birthday = tx.getPropertyKey("birthday");
        geo = tx.getPropertyKey("geo");
        precise = tx.getPropertyKey("precise");
        any = tx.getPropertyKey("any");

        assertEquals(Boolean.class, boolval.dataType());
        assertEquals(byte[].class, barr.dataType());
        assertEquals(Object.class, any.dataType());

        final Calendar c = Calendar.getInstance();
        c.setTime(new SimpleDateFormat("ddMMyyyy").parse("28101978"));
        final Geoshape shape = Geoshape.box(10.0, 10.0, 20.0, 20.0);

        TitanVertex v = tx.addVertex();
        v.property(n(boolval), true);
        v.property(VertexProperty.Cardinality.single, n(birthday), c);
        v.property(VertexProperty.Cardinality.single, n(num), new SpecialInt(10));
        v.property(VertexProperty.Cardinality.single, n(barr), new byte[]{1, 2, 3, 4});
        v.property(VertexProperty.Cardinality.single, n(geo), shape);
        v.property(VertexProperty.Cardinality.single, n(precise), 10.12345);
        v.property(n(any), "Hello");
        v.property(n(any), 10l);
        HashMap<String,Integer> testmap = new HashMap<String, Integer>(1);
        testmap.put("test", 10);
        v.property(n(any), testmap);

        // ######## VERIFICATION ##########
        assertTrue(v.<Boolean>value("boolval"));
        assertEquals(10, v.<SpecialInt>value("num").getValue());
        assertEquals(c, v.value("birthday"));
        assertEquals(4, v.<byte[]>value("barr").length);
        assertEquals(shape, v.<Geoshape>value("geo"));
        assertEquals(10.12345,v.<Precision>value("precise").doubleValue(),0.000001);
        assertCount(3,v.properties("any"));
        for (TitanVertexProperty prop : v.query().labels("any").properties()) {
            Object value = prop.value();
            if (value instanceof String) assertEquals("Hello",value);
            else if (value instanceof Long) assertEquals(10l,value);
            else if (value instanceof Map) {
                HashMap<String,Integer> map = (HashMap<String,Integer>)value;
                assertEquals(1,map.size());
            } else fail();
        }

        clopen();

        v = getV(tx,v);

        // ######## VERIFICATION (copied from above) ##########
        assertTrue(v.<Boolean>value("boolval"));
        assertEquals(10, v.<SpecialInt>value("num").getValue());
        assertEquals(c, v.value("birthday"));
        assertEquals(4, v.<byte[]>value("barr").length);
        assertEquals(shape, v.<Geoshape>value("geo"));
        assertEquals(10.12345,v.<Precision>value("precise").doubleValue(),0.000001);
        assertCount(3, v.properties("any"));
        for (TitanVertexProperty prop : v.query().labels("any").properties()) {
            Object value = prop.value();
            if (value instanceof String) assertEquals("Hello",value);
            else if (value instanceof Long) assertEquals(10l,value);
            else if (value instanceof Map) {
                HashMap<String,Integer> map = (HashMap<String,Integer>)value;
                assertEquals(1,map.size());
            } else fail();
        }
    }

    /**
     * This tests a special scenario under which a schema type is defined in a (management) transaction
     * and then accessed in a concurrent transaction.
     * Also ensures that unique property values are enforced within and across transactions
     */
    @Test
    public void testTransactionalScopeOfSchemaTypes() {
        makeVertexIndexedUniqueKey("domain",String.class);
        finishSchema();

        Vertex v1,v2;
        v1 = tx.addVertex();
        try {
            v1.property(VertexProperty.Cardinality.single, "domain",  "unique1");
        } catch (SchemaViolationException e) {

        } finally {
            tx.rollback();
            tx = null;
        }
        newTx();


        v1 = tx.addVertex();
        v1.property("domain", "unique1");
        try {
            v2 = tx.addVertex();
            v2.property("domain", "unique1");
            fail();
        } catch (SchemaViolationException e) {

        } finally {
            tx.rollback();
            tx = null;
        }
        newTx();

        clopen();
        v1 = tx.addVertex();
        v1.property("domain", "unique1");
        assertCount(1, tx.query().has("domain", "unique1").vertices());
        try {
            v2 = tx.addVertex();
            v2.property("domain", "unique1");
            fail();
        } catch (SchemaViolationException e) {

        } finally {
            tx.rollback();
            tx = null;
        }
        newTx();
    }

    /**
     * Tests the automatic creation of types
     */
    @Test
    public void testAutomaticTypeCreation() {
        assertFalse(tx.containsVertexLabel("person"));
        assertFalse(tx.containsVertexLabel("person"));
        assertFalse(tx.containsRelationType("value"));
        assertNull(tx.getPropertyKey("value"));
        PropertyKey value = tx.getOrCreatePropertyKey("value");
        assertNotNull(value);
        assertTrue(tx.containsRelationType("value"));
        TitanVertex v = tx.addVertex("person");
        assertTrue(tx.containsVertexLabel("person"));
        assertEquals("person",v.label());
        assertFalse(tx.containsRelationType("knows"));
        Edge e = v.addEdge("knows",v);
        assertTrue(tx.containsRelationType("knows"));
        assertNotNull(tx.getEdgeLabel(e.label()));

        clopen(option(AUTO_TYPE),"none");

        assertTrue(tx.containsRelationType("value"));
        assertTrue(tx.containsVertexLabel("person"));
        assertTrue(tx.containsRelationType("knows"));
        v = getV(tx,v);

        //Cannot create new labels
        try {
            tx.addVertex("org");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            v.property("bla", 5);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            v.addEdge("blub",v);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testSchemaNameChange() {
        PropertyKey time = mgmt.makePropertyKey("time").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
        EdgeLabel knows = mgmt.makeEdgeLabel("knows").multiplicity(Multiplicity.MULTI).make();
        mgmt.buildEdgeIndex(knows, "byTime", Direction.BOTH, time);
        mgmt.buildIndex("timeIndex",Vertex.class).addKey(time).buildCompositeIndex();
        mgmt.makeVertexLabel("people").make();
        finishSchema();

        //CREATE SMALL GRAPH
        TitanVertex v = tx.addVertex("people");
        v.property(VertexProperty.Cardinality.single, "time",  5);
        v.addEdge("knows", v, "time", 11);

        newTx();
        v = getOnlyElement(tx.query().has("time",5).vertices());
        assertNotNull(v);
        assertEquals("people", v.label());
        assertEquals(5,v.<Integer>value("time").intValue());
        assertCount(1, v.query().direction(Direction.IN).labels("knows").edges());
        assertCount(1, v.query().direction(Direction.OUT).labels("knows").has("time", 11).edges());
        newTx();

        //UPDATE SCHEMA NAMES

        assertTrue(mgmt.containsRelationType("knows"));
        knows = mgmt.getEdgeLabel("knows");
        mgmt.changeName(knows,"know");
        assertEquals("know",knows.name());

        assertTrue(mgmt.containsRelationIndex(knows,"byTime"));
        RelationTypeIndex rindex = mgmt.getRelationIndex(knows,"byTime");
        assertEquals("byTime",rindex.name());
        mgmt.changeName(rindex,"overTime");
        assertEquals("overTime",rindex.name());

        assertTrue(mgmt.containsVertexLabel("people"));
        VertexLabel vl = mgmt.getVertexLabel("people");
        mgmt.changeName(vl,"person");
        assertEquals("person",vl.name());

        assertTrue(mgmt.containsGraphIndex("timeIndex"));
        TitanGraphIndex gindex = mgmt.getGraphIndex("timeIndex");
        mgmt.changeName(gindex,"byTime");
        assertEquals("byTime",gindex.name());

        finishSchema();

        //VERIFY UPDATES IN MGMT SYSTEM

        assertTrue(mgmt.containsRelationType("know"));
        assertFalse(mgmt.containsRelationType("knows"));
        knows = mgmt.getEdgeLabel("know");

        assertTrue(mgmt.containsRelationIndex(knows,"overTime"));
        assertFalse(mgmt.containsRelationIndex(knows,"byTime"));

        assertTrue(mgmt.containsVertexLabel("person"));
        assertFalse(mgmt.containsVertexLabel("people"));

        assertTrue(mgmt.containsGraphIndex("byTime"));
        assertFalse(mgmt.containsGraphIndex("timeIndex"));

        //VERIFY UPDATES IN TRANSACTION
        newTx();
        v = getOnlyElement(tx.query().has("time",5).vertices());
        assertNotNull(v);
        assertEquals("person", v.label());
        assertEquals(5,v.<Integer>value("time").intValue());
        assertCount(1, v.query().direction(Direction.IN).labels("know").edges());
        assertCount(0,v.query().direction(Direction.IN).labels("knows").edges());
        assertCount(1,v.query().direction(Direction.OUT).labels("know").has("time",11).edges());
    }

    @Test
    public void testIndexUpdatesWithReindexAndRemove() throws InterruptedException, ExecutionException {
        clopen( option(LOG_SEND_DELAY,MANAGEMENT_LOG),new StandardDuration(0,TimeUnit.MILLISECONDS),
                option(KCVSLog.LOG_READ_LAG_TIME,MANAGEMENT_LOG),new StandardDuration(50,TimeUnit.MILLISECONDS),
                option(LOG_READ_INTERVAL,MANAGEMENT_LOG),new StandardDuration(250,TimeUnit.MILLISECONDS)
        );
        //Types without index
        PropertyKey time = mgmt.makePropertyKey("time").dataType(Integer.class).make();
        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.SET).make();
        EdgeLabel friend = mgmt.makeEdgeLabel("friend").multiplicity(Multiplicity.MULTI).make();
        PropertyKey sensor = mgmt.makePropertyKey("sensor").dataType(Double.class).cardinality(Cardinality.LIST).make();
        finishSchema();

        RelationTypeIndex pindex, eindex;
        TitanGraphIndex gindex;

        //Add some sensor & friend data
        TitanVertex v = tx.addVertex();
        for (int i=0;i<10;i++) {
            v.property("sensor", i, "time", i);
            v.property("name", "v" + i);
            TitanVertex o = tx.addVertex();
            v.addEdge("friend", o, "time", i);
        }
        newTx();
        //Indexes should not yet be in use
        v = getV(tx,v);
        evaluateQuery(v.query().keys("sensor").interval("time", 1, 5).orderBy("time", decr),
                PROPERTY,4,1,new boolean[]{false,false},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().keys("sensor").interval("time", 101, 105).orderBy("time", decr),
                PROPERTY,0,1,new boolean[]{false,false},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 1, 5).orderBy("time", decr),
                EDGE,4,1,new boolean[]{false,false},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 101, 105).orderBy("time", decr),
                EDGE,0,1,new boolean[]{false,false},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(tx.query().has("name","v5"),
                ElementCategory.VERTEX,1,new boolean[]{false,true});
        evaluateQuery(tx.query().has("name","v105"),
                ElementCategory.VERTEX,0,new boolean[]{false,true});
        newTx();

        //Create indexes after the fact
        finishSchema();
        sensor = mgmt.getPropertyKey("sensor");
        time = mgmt.getPropertyKey("time");
        name = mgmt.getPropertyKey("name");
        friend = mgmt.getEdgeLabel("friend");
        mgmt.buildPropertyIndex(sensor, "byTime", decr, time);
        mgmt.buildEdgeIndex(friend, "byTime", Direction.OUT, decr, time);
        mgmt.buildIndex("bySensorReading",Vertex.class).addKey(name).buildCompositeIndex();
        finishSchema();
        newTx();
        //Add some sensor & friend data that should already be indexed even though index is not yet enabled
        v = getV(tx,v);
        for (int i=100;i<110;i++) {
            v.property("sensor", i, "time", i);
            v.property("name", "v" + i);
            TitanVertex o = tx.addVertex();
            v.addEdge("friend", o, "time", i);
        }
        tx.commit();
        //Should not yet be able to enable since not yet registered
        pindex = mgmt.getRelationIndex(mgmt.getRelationType("sensor"),"byTime");
        eindex = mgmt.getRelationIndex(mgmt.getRelationType("friend"),"byTime");
        gindex = mgmt.getGraphIndex("bySensorReading");
        try {
            mgmt.updateIndex(pindex, SchemaAction.ENABLE_INDEX);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            mgmt.updateIndex(eindex, SchemaAction.ENABLE_INDEX);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            mgmt.updateIndex(gindex, SchemaAction.ENABLE_INDEX);
            fail();
        } catch (IllegalArgumentException e) {}
        mgmt.commit();


        ManagementUtil.awaitVertexIndexUpdate(graph,"byTime","sensor", 10, TimeUnit.SECONDS);
        ManagementUtil.awaitGraphIndexUpdate(graph,"bySensorReading", 5, TimeUnit.SECONDS);

        finishSchema();
        //Verify new status
        pindex = mgmt.getRelationIndex(mgmt.getRelationType("sensor"),"byTime");
        eindex = mgmt.getRelationIndex(mgmt.getRelationType("friend"),"byTime");
        gindex = mgmt.getGraphIndex("bySensorReading");
        assertEquals(SchemaStatus.REGISTERED, pindex.getIndexStatus());
        assertEquals(SchemaStatus.REGISTERED, eindex.getIndexStatus());
        assertEquals(SchemaStatus.REGISTERED, gindex.getIndexStatus(gindex.getFieldKeys()[0]));
        finishSchema();
        //Simply enable without reindex
        eindex = mgmt.getRelationIndex(mgmt.getRelationType("friend"),"byTime");
        mgmt.updateIndex(eindex, SchemaAction.ENABLE_INDEX);
        finishSchema();
        assertTrue(ManagementSystem.awaitRelationIndexStatus(graph, "byTime", "friend").status(SchemaStatus.ENABLED)
                .timeout(10L, TimeUnit.SECONDS).call().getSucceeded());

        //Reindex the other two
        pindex = mgmt.getRelationIndex(mgmt.getRelationType("sensor"),"byTime");
        mgmt.updateIndex(pindex, SchemaAction.REINDEX);
        finishSchema();
        gindex = mgmt.getGraphIndex("bySensorReading");
        mgmt.updateIndex(gindex, SchemaAction.REINDEX);
        finishSchema();
        waitForReindex(graph, mgmt -> mgmt.getRelationIndex(mgmt.getRelationType("sensor"), "byTime"));
        waitForReindex(graph, mgmt -> mgmt.getGraphIndex("bySensorReading"));

        //Every index should now be enabled
        pindex = mgmt.getRelationIndex(mgmt.getRelationType("sensor"),"byTime");
        eindex = mgmt.getRelationIndex(mgmt.getRelationType("friend"),"byTime");
        gindex = mgmt.getGraphIndex("bySensorReading");
        assertEquals(SchemaStatus.ENABLED, eindex.getIndexStatus());
        assertEquals(SchemaStatus.ENABLED, pindex.getIndexStatus());
        assertEquals(SchemaStatus.ENABLED, gindex.getIndexStatus(gindex.getFieldKeys()[0]));


        //Add some more sensor & friend data
        newTx();
        v = getV(tx,v);
        for (int i=200;i<210;i++) {
            v.property("sensor", i, "time", i);
            v.property("name", "v" + i);
            TitanVertex o = tx.addVertex();
            v.addEdge("friend", o, "time", i);
        }
        newTx();
        //Use indexes now but only see new data for property and graph index
        v = getV(tx,v);
        evaluateQuery(v.query().keys("sensor").interval("time", 1, 5).orderBy("time", decr),
                PROPERTY,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().keys("sensor").interval("time", 101, 105).orderBy("time", decr),
                PROPERTY,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().keys("sensor").interval("time", 201, 205).orderBy("time", decr),
                PROPERTY,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 1, 5).orderBy("time", decr),
                EDGE,0,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 101, 105).orderBy("time", decr),
                EDGE,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 201, 205).orderBy("time", decr),
                EDGE,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(tx.query().has("name","v5"),
                ElementCategory.VERTEX,1,new boolean[]{true,true},"bySensorReading");
        evaluateQuery(tx.query().has("name","v105"),
                ElementCategory.VERTEX,1,new boolean[]{true,true},"bySensorReading");
        evaluateQuery(tx.query().has("name","v205"),
                ElementCategory.VERTEX,1,new boolean[]{true,true},"bySensorReading");

        finishSchema();
        eindex = mgmt.getRelationIndex(mgmt.getRelationType("friend"),"byTime");
        mgmt.updateIndex(eindex, SchemaAction.REINDEX);
        finishSchema();
        waitForReindex(graph, mgmt -> mgmt.getRelationIndex(mgmt.getRelationType("friend"),"byTime"));

        finishSchema();
        newTx();
        //It should now have all the answers
        v = getV(tx,v);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 1, 5).orderBy("time", decr),
                EDGE,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 101, 105).orderBy("time", decr),
                EDGE,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 201, 205).orderBy("time", decr),
                EDGE,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);

        pindex = mgmt.getRelationIndex(mgmt.getRelationType("sensor"),"byTime");
        gindex = mgmt.getGraphIndex("bySensorReading");
        mgmt.updateIndex(pindex,SchemaAction.DISABLE_INDEX);
        mgmt.updateIndex(gindex,SchemaAction.DISABLE_INDEX);
        mgmt.commit();
        tx.commit();

        ManagementUtil.awaitVertexIndexUpdate(graph,"byTime","sensor", 10, TimeUnit.SECONDS);
        ManagementUtil.awaitGraphIndexUpdate(graph,"bySensorReading", 5, TimeUnit.SECONDS);

        finishSchema();

        pindex = mgmt.getRelationIndex(mgmt.getRelationType("sensor"),"byTime");
        gindex = mgmt.getGraphIndex("bySensorReading");
        assertEquals(SchemaStatus.DISABLED, pindex.getIndexStatus());
        assertEquals(SchemaStatus.DISABLED, gindex.getIndexStatus(gindex.getFieldKeys()[0]));
        finishSchema();

        newTx();
        //The two disabled indexes should force full scans
        v = getV(tx,v);
        evaluateQuery(v.query().keys("sensor").interval("time", 1, 5).orderBy("time", decr),
                PROPERTY,4,1,new boolean[]{false,false},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().keys("sensor").interval("time", 101, 105).orderBy("time", decr),
                PROPERTY,4,1,new boolean[]{false,false},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().keys("sensor").interval("time", 201, 205).orderBy("time", decr),
                PROPERTY,4,1,new boolean[]{false,false},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 1, 5).orderBy("time", decr),
                EDGE,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 101, 105).orderBy("time", decr),
                EDGE,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 201, 205).orderBy("time", decr),
                EDGE,4,1,new boolean[]{true,true},tx.getPropertyKey("time"),Order.DESC);
        evaluateQuery(tx.query().has("name","v5"),
                ElementCategory.VERTEX,1,new boolean[]{false,true});
        evaluateQuery(tx.query().has("name","v105"),
                ElementCategory.VERTEX,1,new boolean[]{false,true});
        evaluateQuery(tx.query().has("name","v205"),
                ElementCategory.VERTEX,1,new boolean[]{false,true});

        tx.commit();
        finishSchema();
        pindex = mgmt.getRelationIndex(mgmt.getRelationType("sensor"),"byTime");
        gindex = mgmt.getGraphIndex("bySensorReading");
        mgmt.updateIndex(pindex,SchemaAction.REMOVE_INDEX);
        mgmt.updateIndex(gindex, SchemaAction.REMOVE_INDEX);
        TitanManagement.IndexJobFuture pmetrics = mgmt.getIndexJobStatus(pindex);
        TitanManagement.IndexJobFuture gmetrics = mgmt.getIndexJobStatus(gindex);
        finishSchema();
        waitForReindex(graph, mgmt -> mgmt.getRelationIndex(mgmt.getRelationType("sensor"), "byTime"));
        waitForReindex(graph, mgmt -> mgmt.getGraphIndex("bySensorReading"));
        assertEquals(30, pmetrics.get().getCustom(IndexRemoveJob.DELETED_RECORDS_COUNT));
        assertEquals(30, gmetrics.get().getCustom(IndexRemoveJob.DELETED_RECORDS_COUNT));
    }

    public static void waitForReindex(TitanGraph graph, Function<TitanManagement,TitanIndex> indexRetriever) {
        while (true) {
            TitanManagement mgmt = graph.openManagement();
            try {
                TitanIndex index = indexRetriever.apply(mgmt);
                TitanManagement.IndexJobFuture status = mgmt.getIndexJobStatus(index);
                System.out.println("Index [" + index.name() + (index instanceof RelationTypeIndex ? "@" + ((RelationTypeIndex) index).getType().name() : "") + "] job status: " + status);
                if (status.isDone()) break;
            } finally {
                mgmt.rollback();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Category({ BrittleTests.class })
    @Test
    public void testIndexUpdateSyncWithMultipleInstances() throws InterruptedException {
        clopen( option(LOG_SEND_DELAY,MANAGEMENT_LOG),new StandardDuration(0,TimeUnit.MILLISECONDS),
                option(KCVSLog.LOG_READ_LAG_TIME,MANAGEMENT_LOG),new StandardDuration(50,TimeUnit.MILLISECONDS),
                option(LOG_READ_INTERVAL,MANAGEMENT_LOG),new StandardDuration(250,TimeUnit.MILLISECONDS)
        );

        StandardTitanGraph graph2 = (StandardTitanGraph) TitanFactory.open(config);
        TitanTransaction tx2;

        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).make();
        finishSchema();

        tx.addVertex("name","v1");
        newTx();
        evaluateQuery(tx.query().has("name","v1"),ElementCategory.VERTEX,1,new boolean[]{false,true});
        tx2 = graph2.newTransaction();
        evaluateQuery(tx2.query().has("name","v1"),ElementCategory.VERTEX,1,new boolean[]{false,true});
        //Leave tx2 open to delay acknowledgement

        mgmt.buildIndex("theIndex",Vertex.class).addKey(mgmt.getPropertyKey("name")).buildCompositeIndex();
        mgmt.commit();

        TitanTransaction tx3 = graph2.newTransaction();
        tx3.addVertex("name","v2");
        tx3.commit();
        newTx();
        tx.addVertex("name","v3");
        tx.commit();

        Thread.sleep(2000); //Wait for the index to register in graph2
        finishSchema();
        try {
            mgmt.updateIndex(mgmt.getGraphIndex("theIndex"), SchemaAction.ENABLE_INDEX);
            fail(); //Open tx2 should not make this possible
        } catch (IllegalArgumentException e) {}
        finishSchema();
        tx2.commit(); //Release transaction and wait a little for registration which should make enabling possible
        mgmt.rollback();
        assertTrue(ManagementSystem.awaitGraphIndexStatus(graph, "theIndex").status(SchemaStatus.REGISTERED)
                .timeout(TestGraphConfigs.getSchemaConvergenceTime(TimeUnit.SECONDS), TimeUnit.SECONDS)
                .call().getSucceeded());
        finishSchema();
        mgmt.updateIndex(mgmt.getGraphIndex("theIndex"), SchemaAction.ENABLE_INDEX);
        finishSchema();

        tx2 = graph2.newTransaction();
        tx2.addVertex("name","v4"); //Should be added to index but index not yet enabled
        tx2.commit();

        newTx();
        evaluateQuery(tx.query().has("name","v1"),ElementCategory.VERTEX,0,new boolean[]{true,true},"theIndex");
        evaluateQuery(tx.query().has("name","v2"),ElementCategory.VERTEX,1,new boolean[]{true,true},"theIndex");
        evaluateQuery(tx.query().has("name","v3"),ElementCategory.VERTEX,1,new boolean[]{true,true},"theIndex");
        evaluateQuery(tx.query().has("name","v4"),ElementCategory.VERTEX,1,new boolean[]{true,true},"theIndex");

        Thread.sleep(2000);
        tx2 = graph2.newTransaction();
        evaluateQuery(tx2.query().has("name","v1"),ElementCategory.VERTEX,0,new boolean[]{true,true},"theIndex");
        evaluateQuery(tx2.query().has("name","v2"),ElementCategory.VERTEX,1,new boolean[]{true,true},"theIndex");
        evaluateQuery(tx2.query().has("name","v3"),ElementCategory.VERTEX,1,new boolean[]{true,true},"theIndex");
        evaluateQuery(tx2.query().has("name","v4"),ElementCategory.VERTEX,1,new boolean[]{true,true},"theIndex");
        tx2.commit();

        //Finally test retrieving and closing open instances

        Set<String> openInstances = mgmt.getOpenInstances();
        assertEquals(2,openInstances.size());
        assertTrue(openInstances.contains(graph.getConfiguration().getUniqueGraphId()+"(current)"));
        assertTrue(openInstances.contains(graph2.getConfiguration().getUniqueGraphId()));
        try {
            mgmt.forceCloseInstance(graph.getConfiguration().getUniqueGraphId());
            fail(); //Cannot close current instance
        } catch (IllegalArgumentException e) {}
        mgmt.forceCloseInstance(graph2.getConfiguration().getUniqueGraphId());

        graph2.close();

    }

   /* ==================================================================================
                            ADVANCED
     ==================================================================================*/

    /**
     * Test the correct application of {@link com.thinkaurelius.titan.graphdb.types.system.ImplicitKey}
     * to vertices, edges, and properties.
     *
     * Additionally tests RelationIdentifier since this is closely related to ADJACENT and TITANID implicit keys.
     */
    @Test
    public void testImplicitKey() {
        TitanVertex v = graph.addVertex("name","Dan"), u = graph.addVertex();
        Edge e = v.addEdge("knows",u);
        graph.tx().commit();
        RelationIdentifier eid = (RelationIdentifier)e.id();

        assertEquals(v.id(),v.value(ID_NAME));
        assertEquals(eid,e.value(ID_NAME));
        assertEquals("knows",e.value(LABEL_NAME));
        assertEquals(BaseVertexLabel.DEFAULT_VERTEXLABEL.name(), v.value(LABEL_NAME));
        assertCount(1, v.query().direction(Direction.BOTH).labels("knows").has(ID_NAME, eid).edges());
        assertCount(0, v.query().direction(Direction.BOTH).labels("knows").has(ID_NAME, RelationIdentifier.get(new long[]{4, 5, 6, 7})).edges());
        assertCount(1, v.query().direction(Direction.BOTH).labels("knows").has("~nid", eid.getRelationId()).edges());
        assertCount(0, v.query().direction(Direction.BOTH).labels("knows").has("~nid", 110111).edges());
        //Test edge retrieval
        assertNotNull(getE(graph,eid));
        assertEquals(eid,getE(graph,eid).id());
        //Test adjacent constraint
        assertEquals(1, v.query().direction(BOTH).has("~adjacent", u.id()).count());
        assertCount(1, v.query().direction(BOTH).has("~adjacent", (int) getId(u)).edges());
        try {
            //Not a valid vertex
             assertCount(0, v.query().direction(BOTH).has("~adjacent", 110111).edges());
            fail();
        } catch (IllegalArgumentException ex) {}

    }

    @Test
    public void testArrayEqualityUsingImplicitKey() {
        TitanVertex v = graph.addVertex();

        byte singleDimension[]       = new byte[] { 127, 0, 0, 1 };
        byte singleDimensionCopy[] = new byte[] { 127, 0, 0, 1 };
        final String singlePropName = "single";

        v.property(singlePropName, singleDimension);

        assertEquals(1, Iterables.size(graph.query().has(singlePropName, singleDimension).vertices()));
        assertEquals(1, Iterables.size(graph.query().has(singlePropName, singleDimensionCopy).vertices()));

        graph.tx().commit();

        assertEquals(1, Iterables.size(graph.query().has(singlePropName, singleDimension).vertices()));
        assertEquals(1, Iterables.size(graph.query().has(singlePropName, singleDimensionCopy).vertices()));

        byte multiDimension[][]     = new byte[1][1];
        multiDimension[0][0]        = (byte)42;
        byte multiDimensionCopy[][] = new byte[1][1];
        multiDimensionCopy[0][0]    = (byte)42;
        final String multiPropName  = "multi";

        v = graph.addVertex();

        v.property(multiPropName, multiDimension);

        assertEquals(1, Iterables.size(graph.query().has(multiPropName, multiDimension).vertices()));
        assertEquals(1, Iterables.size(graph.query().has(multiPropName, multiDimensionCopy).vertices()));

        graph.tx().commit();

        assertEquals(1, Iterables.size(graph.query().has(multiPropName, multiDimension).vertices()));
        assertEquals(1, Iterables.size(graph.query().has(multiPropName, multiDimensionCopy).vertices()));

    }

    /**
     * Tests that self-loop edges are handled and counted correctly
     */
    @Test
    public void testSelfLoop() {
        TitanVertex v = tx.addVertex();
        v.addEdge("self", v);
        assertCount(1, v.query().direction(Direction.OUT).labels("self").edges());
        assertCount(1, v.query().direction(Direction.IN).labels("self").edges());
        assertCount(2, v.query().direction(Direction.BOTH).labels("self").edges());
        clopen();
        v = getV(tx,v);
        assertNotNull(v);
        assertCount(1, v.query().direction(Direction.IN).labels("self").edges());
        assertCount(1, v.query().direction(Direction.OUT).labels("self").edges());
        assertCount(1, v.query().direction(Direction.IN).labels("self").edges());
        assertCount(2, v.query().direction(Direction.BOTH).labels("self").edges());
    }

    /**
     * Tests that elements can be accessed beyond their transactional boundaries if they
     * are bound to single-threaded graph transactions
     */
    @Test
    public void testThreadBoundTx() {
        PropertyKey t = mgmt.makePropertyKey("type").dataType(Integer.class).make();
        mgmt.buildIndex("etype",Edge.class).addKey(t).buildCompositeIndex();
        ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("friend")).sortKey(t).make();
        finishSchema();

        TitanVertex v1 = graph.addVertex("name", "Vertex1","age", 35);
        TitanVertex v2 = graph.addVertex("name", "Vertex2","age", 45);
        TitanVertex v3 = graph.addVertex("name", "Vertex3","age", 55);

        Edge e1 = v1.addEdge("knows", v2, "time", 5);
        Edge e2 = v2.addEdge("knows", v3, "time", 15);
        Edge e3 = v3.addEdge("knows", v1, "time", 25);
        Edge e4 = v2.addEdge("friend", v2, "type", 1);
        for (TitanVertex v : new TitanVertex[]{v1, v2, v3}) {
            assertCount(2, v.query().direction(Direction.BOTH).labels("knows").edges());
            assertCount(1, v.query().direction(Direction.OUT).labels("knows").edges());
            TitanEdge tmpE = getOnlyElement(v.query().direction(Direction.OUT).labels("knows").edges());
            assertEquals(5, tmpE.<Integer>value("time") % 10);
        }
        e3.property("time", 35);
        assertEquals(35, e3.<Integer>value("time").intValue());

        v1.addEdge("friend", v2, "type", 0);
        graph.tx().commit();
        e4.property("type", 2);
        TitanEdge ef = getOnlyElement(v1.query().direction(Direction.OUT).labels("friend").edges());
        assertEquals(ef, (Edge)getOnlyElement(graph.query().has("type", 0).edges()));
        ef.property("type", 1);
        graph.tx().commit();

        assertEquals(35, e3.<Integer>value("time").intValue());
        e3 = getE(graph,e3);
        e3.property("time", 45);
        assertEquals(45, e3.<Integer>value("time").intValue());

        assertEquals(15, e2.<Integer>value("time").intValue());
        e2.property("time", 25);
        assertEquals(25, e2.<Integer>value("time").intValue());

        assertEquals(35, v1.<Integer>value("age").intValue());
        assertEquals(55, v3.<Integer>value("age").intValue());
        v3.property("age", 65);
        assertEquals(65, v3.<Integer>value("age").intValue());
        e1 = getE(graph,e1);

        for (TitanVertex v : new TitanVertex[]{v1, v2, v3}) {
            assertCount(2, v.query().direction(Direction.BOTH).labels("knows").edges());
            assertCount(1, v.query().direction(Direction.OUT).labels("knows").edges());
            assertEquals(5, getOnlyElement(v.query().direction(Direction.OUT).labels("knows").edges()).<Integer>value("time").intValue() % 10);
        }

        graph.tx().commit();

        VertexProperty prop = v1.properties().next();
        assertTrue(getId(prop)>0);
        prop = (VertexProperty) ((Iterable)graph.multiQuery((TitanVertex)v1).properties().values().iterator().next()).iterator().next();
        assertTrue(getId(prop)>0);

        assertEquals(45, e3.<Integer>value("time").intValue());
        assertEquals(5, e1.<Integer>value("time").intValue());

        assertEquals(35, v1.<Integer>value("age").intValue());
        assertEquals(65, v3.<Integer>value("age").intValue());

        for (TitanVertex v : new TitanVertex[]{v1, v2, v3}) {
            assertCount(2, v.query().direction(Direction.BOTH).labels("knows").edges());
            assertCount(1, v.query().direction(Direction.OUT).labels("knows").edges());
            assertEquals(5, getOnlyElement(v.query().direction(Direction.OUT).labels("knows").edges()).<Integer>value("time").intValue() % 10);
        }

        graph.tx().commit();

        v1 = graph.addVertex();
        v2 = graph.addVertex();
        v1.addEdge("knows",v2);
        graph.tx().commit();
        v3 = graph.addVertex();
        Edge e = v1.addEdge("knows",v3);
        assertFalse(e.property("age").isPresent());
    }

    @Test
    public void testStaleVertex() {
        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).make();
        PropertyKey age = mgmt.makePropertyKey("age").dataType(Integer.class).make();
        mgmt.buildIndex("byName", Vertex.class).addKey(name).unique().buildCompositeIndex();
        finishSchema();


        TitanVertex cartman = graph.addVertex("name","cartman", "age", 10);
        TitanVertex stan = graph.addVertex("name","stan","age",8);

        graph.tx().commit();

        cartman = getOnlyElement(graph.query().has("name", "cartman").vertices());

        graph.tx().commit();

        TitanVertexProperty p = (TitanVertexProperty)cartman.properties().next();
        assertTrue(((Long)p.longId())>0);
        graph.tx().commit();
    }

    /**
     * Verifies transactional isolation and internal vertex existence checking
     */
    @Test
    public void testTransactionIsolation() {
        // Create edge label before attempting to write it from concurrent transactions
        makeLabel("knows");
        finishSchema();

        TitanTransaction tx1 = graph.newTransaction();
        TitanTransaction tx2 = graph.newTransaction();

        //Verify that using vertices across transactions is prohibited
        TitanVertex v11 = tx1.addVertex();
        TitanVertex v12 = tx1.addVertex();
        v11.addEdge("knows",v12);

        TitanVertex v21 = tx2.addVertex();
        try {
            v21.addEdge("knows", v11);
            fail();
        } catch (IllegalStateException e) {
        }
        TitanVertex v22 = tx2.addVertex();
        v21.addEdge("knows", v22);
        tx2.commit();
        try {
            v22.addEdge("knows", v21);
            fail();
        } catch (IllegalStateException e) {
        }
        tx1.rollback();
        try {
            v11.property(VertexProperty.Cardinality.single, "test",  5);
            fail();
        } catch (IllegalStateException e) {
        }

        //Test unidirected edge with and without internal existence check
        newTx();
        v21 = getV(tx,v21);
        tx.makeEdgeLabel("link").unidirected().make();
        TitanVertex v3 = tx.addVertex();
        v21.addEdge("link", v3);
        newTx();
        v21 = getV(tx,v21);
        v3 = getOnlyElement(v21.query().direction(Direction.OUT).labels("link").vertices());
        assertFalse(((TitanVertex)v3).isRemoved());
        v3.remove();
        newTx();
        v21 = getV(tx,v21);
        v3 = getOnlyElement(v21.query().direction(Direction.OUT).labels("link").vertices());
        assertFalse(((TitanVertex)v3).isRemoved());
        newTx();

        TitanTransaction tx3 = graph.buildTransaction().checkInternalVertexExistence(true).start();
        v21 = getV(tx3,v21);
        v3 = getOnlyElement(v21.query().direction(Direction.OUT).labels("link").vertices());
        assertTrue(((TitanVertex) v3).isRemoved());
        tx3.commit();
    }


    /**
     * Tests multi-valued properties with special focus on indexing and incident unidirectional edges
     * which is not tested in {@link #testSchemaTypes()}
     *
     * -->TODO: split and move this into other test cases: ordering to query, indexing to index
     */
    @Test
    public void testMultivaluedVertexProperty() {
        /*
         * Constant test data
         *
         * The values list below must have at least two elements. The string
         * literals were chosen arbitrarily and have no special significance.
         */
        final String foo = "foo", bar = "bar", weight = "weight";
        final List<String> values =
                ImmutableList.of("four", "score", "and", "seven");
        assertTrue("Values list must have multiple elements for this test to make sense",
                2 <= values.size());

        // Create property with name pname and a vertex
        PropertyKey w = makeKey(weight,Integer.class);
        PropertyKey f = ((StandardPropertyKeyMaker)mgmt.makePropertyKey(foo)).dataType(String.class).cardinality(Cardinality.LIST).sortKey(w).sortOrder(Order.DESC).make();
        mgmt.buildIndex(foo,Vertex.class).addKey(f).buildCompositeIndex();
        PropertyKey b = mgmt.makePropertyKey(bar).dataType(String.class).cardinality(Cardinality.LIST).make();
        mgmt.buildIndex(bar,Vertex.class).addKey(b).buildCompositeIndex();
        finishSchema();

        TitanVertex v = tx.addVertex();

        // Insert prop values
        int i=0;
        for (String s : values) {
            v.property(foo, s, weight,++i);
            v.property(bar, s, weight,i);
        }

        //Verify correct number of properties
        assertCount(values.size(), v.properties(foo));
        assertCount(values.size(), v.properties(bar));
        //Verify order
        for (String prop : new String[]{foo,bar}) {
            int sum = 0;
            int index = values.size();
            for (TitanVertexProperty<String> p : v.query().labels(foo).properties()) {
                assertTrue(values.contains(p.value()));
                int wint = p.value(weight);
                sum+=wint;
                if (prop==foo) assertEquals(index,wint);
                index--;
            }
            assertEquals(values.size()*(values.size()+1)/2,sum);
        }


        assertCount(1, tx.query().has(foo, values.get(1)).vertices());
        assertCount(1, tx.query().has(foo, values.get(3)).vertices());

        assertCount(1, tx.query().has(bar, values.get(1)).vertices());
        assertCount(1, tx.query().has(bar, values.get(3)).vertices());

        // Check that removing properties works
        v.properties(foo).remove();
        // Check that the properties were actually deleted from v
        assertEmpty(v.properties(foo));

        // Reopen database
        clopen();

        assertCount(0, tx.query().has(foo, values.get(1)).vertices());
        assertCount(0, tx.query().has(foo, values.get(3)).vertices());

        assertCount(1, tx.query().has(bar, values.get(1)).vertices());
        assertCount(1, tx.query().has(bar, values.get(3)).vertices());

        // Retrieve and check our test vertex
        v = getV(tx,v);
        assertEmpty(v.properties(foo));
        assertCount(values.size(), v.properties(bar));
        // Reinsert prop values
        for (String s : values) {
            v.property(foo, s);
        }
        assertCount(values.size(), v.properties(foo));

        // Check that removing properties works
        v.properties(foo).remove();
        // Check that the properties were actually deleted from v
        assertEmpty(v.properties(foo));
    }

    @Test
    public void testLocalGraphConfiguration() {
        setIllegalGraphOption(STORAGE_READONLY, ConfigOption.Type.LOCAL, true);
    }

    @Test
    public void testMaskableGraphConfig() {
        setAndCheckGraphOption(DB_CACHE, ConfigOption.Type.MASKABLE, true, false);
    }

    @Test
    public void testGlobalGraphConfig() {
        setAndCheckGraphOption(SYSTEM_LOG_TRANSACTIONS, ConfigOption.Type.GLOBAL, true, false);
    }

    @Test
    public void testGlobalOfflineGraphConfig() {
        setAndCheckGraphOption(DB_CACHE_TIME, ConfigOption.Type.GLOBAL_OFFLINE, 500L, 777L);
    }

    @Test
    public void testFixedGraphConfig() {
        setIllegalGraphOption(INITIAL_TITAN_VERSION, ConfigOption.Type.FIXED, "foo");
    }

    @Test
    public void testManagedOptionMasking() throws BackendException {
        // Can't use clopen(...) for this test, because it's aware local vs global option types and
        // uses ManagementSystem where necessary.  We want to simulate an erroneous attempt to
        // override global options by tweaking the local config file (ignoring ManagementSystem),
        // so we have to bypass clopen(...).
        //clopen(
        //    option(ALLOW_STALE_CONFIG), false,
        //    option(ATTRIBUTE_ALLOW_ALL_SERIALIZABLE), false);

        // Check this test's assumptions about option default values

        StandardDuration customCommitTime = new StandardDuration(456L, TimeUnit.MILLISECONDS);
        Preconditions.checkState(true == ALLOW_STALE_CONFIG.getDefaultValue());
        Preconditions.checkState(ALLOW_STALE_CONFIG.getType().equals(ConfigOption.Type.MASKABLE));
        Preconditions.checkState(!customCommitTime.equals(MAX_COMMIT_TIME.getDefaultValue()));

        // Disallow managed option masking and verify exception at graph startup
        close();
        WriteConfiguration wc = getConfiguration();
        wc.set(ConfigElement.getPath(ALLOW_STALE_CONFIG), false);
        wc.set(ConfigElement.getPath(MAX_COMMIT_TIME), customCommitTime.getLength(TimeUnit.MILLISECONDS));
        try {
            graph = (StandardTitanGraph) TitanFactory.open(wc);
            fail("Masking managed config options should be disabled in this configuration");
        } catch (TitanConfigurationException e) {
            // Exception should cite the problematic setting's full name
            assertTrue(e.getMessage().contains(ConfigElement.getPath(MAX_COMMIT_TIME)));
        }

        // Allow managed option masking (default config again) and check that the local value is ignored and
        // that no exception is thrown
        close();
        wc = getConfiguration();
        wc.set(ConfigElement.getPath(ALLOW_STALE_CONFIG), true);
        wc.set(ConfigElement.getPath(MAX_COMMIT_TIME), customCommitTime.getLength(TimeUnit.MILLISECONDS));
        graph = (StandardTitanGraph) TitanFactory.open(wc);
        // Local value should be overridden by the default that already exists in the backend
        assertEquals(MAX_COMMIT_TIME.getDefaultValue(), graph.getConfiguration().getMaxCommitTime());

        // Wipe the storage backend
        graph.getBackend().clearStorage();
        try {
            graph.close();
        } catch (Throwable t) {
            log.debug("Swallowing throwable during shutdown after clearing backend storage", t);
        }

        // Bootstrap a new DB with managed option masking disabled
        wc = getConfiguration();
        wc.set(ConfigElement.getPath(ALLOW_STALE_CONFIG), false);
        graph = (StandardTitanGraph) TitanFactory.open(wc);
        close();

        // Check for expected exception
        wc = getConfiguration();
        wc.set(ConfigElement.getPath(MAX_COMMIT_TIME), customCommitTime.getLength(TimeUnit.MILLISECONDS));
        try {
            graph = (StandardTitanGraph) TitanFactory.open(wc);
            fail("Masking managed config options should be disabled in this configuration");
        } catch (TitanConfigurationException e) {
            // Exception should cite the problematic setting's full name
            assertTrue(e.getMessage().contains(ConfigElement.getPath(MAX_COMMIT_TIME)));
        }

        // Now check that ALLOW_STALE_CONFIG is actually MASKABLE -- enable it in the local config
        wc = getConfiguration();
        wc.set(ConfigElement.getPath(ALLOW_STALE_CONFIG), true);
        wc.set(ConfigElement.getPath(MAX_COMMIT_TIME), customCommitTime.getLength(TimeUnit.MILLISECONDS));
        graph = (StandardTitanGraph) TitanFactory.open(wc);
        // Local value should be overridden by the default that already exists in the backend
        assertEquals(MAX_COMMIT_TIME.getDefaultValue(), graph.getConfiguration().getMaxCommitTime());
    }

    @Test
    public void testTransactionConfiguration() {
        // Superficial tests for a few transaction builder methods

        // Test read-only transaction
        TitanTransaction readOnlyTx = graph.buildTransaction().readOnly().start();
        try {
            readOnlyTx.addVertex();
            readOnlyTx.commit();
            fail("Read-only transactions should not be able to add a vertex and commit");
        } catch (Throwable t) {
            if (readOnlyTx.isOpen())
                readOnlyTx.rollback();
        }

        // Test custom log identifier
        String logID = "spam";
        StandardTitanTx customLogIDTx = (StandardTitanTx)graph.buildTransaction().logIdentifier(logID).start();
        assertEquals(logID, customLogIDTx.getConfiguration().getLogIdentifier());
        customLogIDTx.rollback();

        // Test timestamp
        long customTimestamp = -42L;
        StandardTitanTx customTimeTx = (StandardTitanTx)graph.buildTransaction().commitTime(customTimestamp, TimeUnit.MILLISECONDS).start();
        assertTrue(customTimeTx.getConfiguration().hasCommitTime());
        assertEquals(customTimestamp, customTimeTx.getConfiguration().getCommitTime().getTimestamp(TimeUnit.MILLISECONDS));
        customTimeTx.rollback();
    }

    private <T> void setAndCheckGraphOption(ConfigOption<T> opt, ConfigOption.Type requiredType, T firstValue, T secondValue) {
        // Sanity check: make sure the Type of the configoption is what we expect
        Preconditions.checkState(opt.getType().equals(requiredType));
        final EnumSet<ConfigOption.Type> allowedTypes =
                EnumSet.of(ConfigOption.Type.GLOBAL,
                           ConfigOption.Type.GLOBAL_OFFLINE,
                           ConfigOption.Type.MASKABLE);
        Preconditions.checkState(allowedTypes.contains(opt.getType()));

        // Sanity check: it's kind of pointless for the first and second values to be identical
        Preconditions.checkArgument(!firstValue.equals(secondValue));

        // Get full string path of config option
        final String path = ConfigElement.getPath(opt);

        // Set and check initial value before and after database restart
        mgmt.set(path, firstValue);
        assertEquals(firstValue.toString(), mgmt.get(path));
        // Close open tx first.  This is specific to BDB + GLOBAL_OFFLINE.
        // Basically: the BDB store manager throws a fit if shutdown is called
        // with one or more transactions still open, and GLOBAL_OFFLINE calls
        // shutdown on our behalf when we commit this change.
        tx.rollback();
        mgmt.commit();
        clopen();
        // Close tx again following clopen
        tx.rollback();
        assertEquals(firstValue.toString(), mgmt.get(path));

        // Set and check updated value before and after database restart
        mgmt.set(path, secondValue);
        assertEquals(secondValue.toString(), mgmt.get(path));
        mgmt.commit();
        clopen();
        tx.rollback();
        assertEquals(secondValue.toString(), mgmt.get(path));

        // Open a separate graph "g2"
        TitanGraph g2 = TitanFactory.open(config);
        TitanManagement m2 = g2.openManagement();
        assertEquals(secondValue.toString(), m2.get(path));

        // GLOBAL_OFFLINE options should be unmodifiable with g2 open
        if (opt.getType().equals(ConfigOption.Type.GLOBAL_OFFLINE)) {
            try {
                mgmt.set(path, firstValue);
                mgmt.commit();
                fail("Option " + path + " with type "+ ConfigOption.Type.GLOBAL_OFFLINE + " should not be modifiable with concurrent instances");
            } catch (RuntimeException e) {
                log.debug("Caught expected exception", e);
            }
            assertEquals(secondValue.toString(), mgmt.get(path));
        // GLOBAL and MASKABLE should be modifiable even with g2 open
        } else {
            mgmt.set(path, firstValue);
            assertEquals(firstValue.toString(), mgmt.get(path));
            mgmt.commit();
            clopen();
            assertEquals(firstValue.toString(), mgmt.get(path));
        }

        m2.rollback();
        g2.close();
    }

    private <T> void setIllegalGraphOption(ConfigOption<T> opt, ConfigOption.Type requiredType, T attemptedValue) {
        // Sanity check: make sure the Type of the configoption is what we expect
        final ConfigOption.Type type = opt.getType();
        Preconditions.checkState(type.equals(requiredType));
        Preconditions.checkArgument(requiredType.equals(ConfigOption.Type.LOCAL) ||
                                    requiredType.equals(ConfigOption.Type.FIXED));

        // Get full string path of config option
        final String path = ConfigElement.getPath(opt);


        // Try to read the option
        try {
            mgmt.get(path);
        } catch (Throwable t) {
            log.debug("Caught expected exception", t);
        }

        // Try to modify the option
        try {
            mgmt.set(path, attemptedValue);
            mgmt.commit();
            fail("Option " +  path + " with type " + type + " should not be modifiable in the persistent graph config");
        } catch (Throwable t) {
            log.debug("Caught expected exception", t);
        }
    }


   /* ==================================================================================
                            CONSISTENCY
     ==================================================================================*/

    /**
     * Tests the correct application of ConsistencyModifiers across transactional boundaries
     */
    @Test
    public void testConsistencyEnforcement() {
        PropertyKey uid = makeVertexIndexedUniqueKey("uid",Integer.class);
        PropertyKey name = makeKey("name",String.class);
        mgmt.setConsistency(uid, ConsistencyModifier.LOCK);
        mgmt.setConsistency(name, ConsistencyModifier.LOCK);
        mgmt.setConsistency(mgmt.getGraphIndex("uid"),ConsistencyModifier.LOCK);
        EdgeLabel knows = mgmt.makeEdgeLabel("knows").multiplicity(Multiplicity.SIMPLE).make();
        EdgeLabel spouse = mgmt.makeEdgeLabel("spouse").multiplicity(Multiplicity.ONE2ONE).make();
        EdgeLabel connect = mgmt.makeEdgeLabel("connect").multiplicity(Multiplicity.MULTI).make();
        EdgeLabel related = mgmt.makeEdgeLabel("related").multiplicity(Multiplicity.MULTI).make();
        mgmt.setConsistency(knows, ConsistencyModifier.LOCK);
        mgmt.setConsistency(spouse, ConsistencyModifier.LOCK);
        mgmt.setConsistency(related,ConsistencyModifier.FORK);
        finishSchema();

        name = tx.getPropertyKey("name");
        connect = tx.getEdgeLabel("connect");
        related = tx.getEdgeLabel("related");

        TitanVertex v1 = tx.addVertex("uid",1);
        TitanVertex v2 = tx.addVertex("uid",2);
        TitanVertex v3 = tx.addVertex("uid",3);

        Edge e1 = v1.addEdge(connect.name(),v2, name.name(),"e1");
        Edge e2 = v1.addEdge(related.name(),v2, name.name(),"e2");

        newTx();
        v1 = getV(tx,v1);
        /*
         ==== check fork, no fork behavior
         */
        long e1id = getId(e1);
        long e2id = getId(e2);
        e1 = getOnlyElement(v1.query().direction(Direction.OUT).labels("connect").edges());
        assertEquals("e1",e1.value("name"));
        assertEquals(e1id,getId(e1));
        e2 = getOnlyElement(v1.query().direction(Direction.OUT).labels("related").edges());
        assertEquals("e2",e2.value("name"));
        assertEquals(e2id,getId(e2));
        //Update edges - one should simply update, the other fork
        e1.property("name","e1.2");
        e2.property("name","e2.2");

        newTx();
        v1 = getV(tx,v1);

        e1 = getOnlyElement(v1.query().direction(Direction.OUT).labels("connect").edges());
        assertEquals("e1.2",e1.value("name"));
        assertEquals(e1id,getId(e1)); //should have same id
        e2 = getOnlyElement(v1.query().direction(Direction.OUT).labels("related").edges());
        assertEquals("e2.2",e2.value("name"));
        assertNotEquals(e2id,getId(e2)); //should have different id since forked

        clopen();

        /*
         === check cross transaction
         */
        final Random random = new Random();
        final long vids[] = {getId(v1),getId(v2),getId(v3)};
        //1) Index uniqueness
        executeLockConflictingTransactionJobs(graph,new TransactionJob() {
            private int pos = 0;
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex u = getV(tx,vids[pos++]);
                u.property(VertexProperty.Cardinality.single, "uid", 5);
            }
        });
        //2) Property out-uniqueness
        executeLockConflictingTransactionJobs(graph,new TransactionJob() {
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex u = getV(tx,vids[0]);
                u.property(VertexProperty.Cardinality.single, "name", "v"+random.nextInt(10));
            }
        });
        //3) knows simpleness
        executeLockConflictingTransactionJobs(graph,new TransactionJob() {
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex u1 = getV(tx,vids[0]), u2 = getV(tx,vids[1]);
                u1.addEdge("knows",u2);
            }
        });
        //4) knows one2one (in 2 separate configurations)
        executeLockConflictingTransactionJobs(graph,new TransactionJob() {
            private int pos = 1;
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex u1 = getV(tx,vids[0]),
                        u2 = getV(tx,vids[pos++]);
                u1.addEdge("spouse",u2);
            }
        });
        executeLockConflictingTransactionJobs(graph,new TransactionJob() {
            private int pos = 1;
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex u1 = getV(tx,vids[pos++]),
                        u2 = getV(tx,vids[0]);
                u1.addEdge("spouse",u2);
            }
        });

        //######### TRY INVALID CONSISTENCY
        try {
            //Fork does not apply to constrained types
            mgmt.setConsistency(mgmt.getPropertyKey("name"),ConsistencyModifier.FORK);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    /**
     * A piece of logic to be executed in a transactional context
     */
    private static interface TransactionJob {
        public void run(TitanTransaction tx);
    }

    /**
     * Executes a transaction job in two parallel transactions under the assumptions that the two transactions
     * should conflict and the one committed later should throw a locking exception.
     *
     * @param graph
     * @param job
     */
    private void executeLockConflictingTransactionJobs(TitanGraph graph, TransactionJob job) {
        TitanTransaction tx1 = graph.newTransaction();
        TitanTransaction tx2 = graph.newTransaction();
        job.run(tx1);
        job.run(tx2);
        /*
         * Under pessimistic locking, tx1 should abort and tx2 should commit.
         * Under optimistic locking, tx1 may commit and tx2 may abort.
         */
        if (isLockingOptimistic()) {
            tx1.commit();
            try {
                tx2.commit();
                fail("Storage backend does not abort conflicting transactions");
            } catch (TitanException e) {
            }
        } else {
            try {
                tx1.commit();
                fail("Storage backend does not abort conflicting transactions");
            } catch (TitanException e) {
            }
            tx2.commit();
        }
    }

    @Test
    public void testConcurrentConsistencyEnforcement() throws Exception {
        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        TitanGraphIndex nameIndex = mgmt.buildIndex("name", Vertex.class)
                .addKey(name).unique().buildCompositeIndex();
        mgmt.setConsistency(nameIndex, ConsistencyModifier.LOCK);
        EdgeLabel married = mgmt.makeEdgeLabel("married").multiplicity(Multiplicity.ONE2ONE).make();
        mgmt.setConsistency(married,ConsistencyModifier.LOCK);
        EdgeLabel friend = mgmt.makeEdgeLabel("friend").multiplicity(Multiplicity.MULTI).make();
        finishSchema();

        TitanVertex baseV = tx.addVertex("name","base");
        newTx();
        final long baseVid = getId(baseV);
        final String nameA = "a", nameB = "b";
        final int parallelThreads = 4;
        final AtomicInteger totalExe = new AtomicInteger();

        int numSuccess = executeParallelTransactions(new TransactionJob() {
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex a = tx.addVertex();
                TitanVertex base = getV(tx,baseVid);
                base.addEdge("married",a);
            }
        },parallelThreads);

        assertTrue("At most 1 tx should succeed: " + numSuccess,numSuccess<=1);

        numSuccess = executeParallelTransactions(new TransactionJob() {
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex a = tx.addVertex("name",nameA);
                TitanVertex b = tx.addVertex("name",nameB);
                b.addEdge("friend",b);
            }
        },parallelThreads);

        newTx();
        long numA = Iterables.size(tx.query().has("name", nameA).vertices());
        long numB = Iterables.size(tx.query().has("name", nameB).vertices());
//        System.out.println(numA + " - " + numB);
        assertTrue("At most 1 tx should succeed: " + numSuccess,numSuccess<=1);
        assertTrue(numA<=1);
        assertTrue(numB<=1);
    }

    private void failTransactionOnCommit(final TransactionJob job) {
        TitanTransaction tx = graph.newTransaction();
        try {
            job.run(tx);
            tx.commit();
            fail();
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            if (tx.isOpen()) tx.rollback();
        }
    }

    private int executeSerialTransaction(final TransactionJob job, int number) {
        final AtomicInteger txSuccess = new AtomicInteger(0);
        for (int i = 0; i < number; i++) {
            TitanTransaction tx = graph.newTransaction();
            try {
                job.run(tx);
                tx.commit();
                txSuccess.incrementAndGet();
            } catch (Exception ex) {
                tx.rollback();
                ex.printStackTrace();
            }
        }
        return txSuccess.get();
    }

    private int executeParallelTransactions(final TransactionJob job, int number) {
        final CountDownLatch startLatch = new CountDownLatch(number);
        final CountDownLatch finishLatch = new CountDownLatch(number);
        final AtomicInteger txSuccess = new AtomicInteger(0);
        for (int i = 0; i < number; i++) {
            new Thread() {
                public void run() {
                    awaitAllThreadsReady();
                    TitanTransaction tx = graph.newTransaction();
                    try {
                        job.run(tx);
                        tx.commit();
                        txSuccess.incrementAndGet();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        if (tx.isOpen()) tx.rollback();
                    } finally {
                        finishLatch.countDown();
                    }
                }

                private void awaitAllThreadsReady() {
                    startLatch.countDown();
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        try {
            finishLatch.await(10000,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return txSuccess.get();
    }

   /* ==================================================================================
                            VERTEX CENTRIC QUERIES
     ==================================================================================*/

   @Test
   public void testVertexCentricQuery() {
       makeVertexIndexedUniqueKey("name",String.class);
       PropertyKey time = makeKey("time",Integer.class);
       PropertyKey weight = makeKey("weight",Precision.class);

       EdgeLabel author = mgmt.makeEdgeLabel("author").multiplicity(Multiplicity.MANY2ONE).unidirected().make();

       ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("connect")).sortKey(time).make();
       ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("connectDesc")).sortKey(time).sortOrder(Order.DESC).make();
       ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("friend")).sortKey(weight, time).sortOrder(Order.ASC).signature(author).make();
       ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("friendDesc")).sortKey(weight, time).sortOrder(Order.DESC).signature(author).make();
       ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("knows")).sortKey(author, weight).make();
       mgmt.makeEdgeLabel("follows").make();
       finishSchema();

       TitanVertex v = tx.addVertex("name","v");
       TitanVertex u = tx.addVertex("name","u");
       int noVertices = 10000;
       assertEquals(0,(noVertices-1)%3);
       TitanVertex[] vs = new TitanVertex[noVertices];
       for (int i = 1; i < noVertices; i++) {
           vs[i] = tx.addVertex("name", "v" + i);
       }
       EdgeLabel[] labelsV = {tx.getEdgeLabel("connect"),tx.getEdgeLabel("friend"),tx.getEdgeLabel("knows")};
       EdgeLabel[] labelsU = {tx.getEdgeLabel("connectDesc"),tx.getEdgeLabel("friendDesc"),tx.getEdgeLabel("knows")};
       for (int i = 1; i < noVertices; i++) {
           for (TitanVertex vertex : new TitanVertex[]{v,u}) {
               for (Direction d : new Direction[]{OUT,IN}) {
                   EdgeLabel label = vertex==v?labelsV[i%3]:labelsU[i%3];
                   TitanEdge e = d==OUT?vertex.addEdge(n(label),vs[i]):
                           vs[i].addEdge(n(label),vertex);
                   e.property("time", i);
                   e.property("weight", i % 4 + 0.5);
                   e.property("name", "e" + i);
                   e.property("author", i % 5 == 0 ? v : vs[i % 5]);
               }
           }
       }
       int edgesPerLabel = noVertices/3;



       VertexList vl;
       Map<TitanVertex, Iterable<TitanEdge>> results;
       Map<TitanVertex, Iterable<TitanVertexProperty>> results2;
       TitanVertex[] qvs;
       int lastTime;
       Iterator<? extends Edge> outer;

       clopen();

       long[] vidsubset = new long[31 - 3];
       for (int i = 0; i < vidsubset.length; i++) vidsubset[i] = vs[i + 3].longId();
       Arrays.sort(vidsubset);

       //##################################################
       //Queries from Cache
       //##################################################
       clopen();
       for (int i = 1; i < noVertices; i++) vs[i] = getV(tx,vs[i].longId());
       v = getV(tx,v.longId());
       u = getV(tx,u.longId());
       qvs = new TitanVertex[]{vs[6], vs[9], vs[12], vs[15], vs[60]};

       //To trigger queries from cache (don't copy!!!)
       assertCount(2 * (noVertices - 1), v.query().direction(Direction.BOTH).edges());


       assertEquals(10, size(v.query().labels("connect").limit(10).vertices()));
       assertEquals(10, size(u.query().labels("connectDesc").limit(10).vertices()));
       assertEquals(10, size(v.query().labels("connect").has("time", Cmp.GREATER_THAN, 30).limit(10).vertices()));
       assertEquals(10, size(u.query().labels("connectDesc").has("time", Cmp.GREATER_THAN, 30).limit(10).vertices()));

       lastTime = 0;
       for (TitanEdge e : (Iterable<TitanEdge>)v.query().labels("connect").direction(OUT).limit(20).edges()) {
           int nowTime = e.value("time");
           assertTrue(lastTime + " vs. " + nowTime, lastTime <= nowTime);
           lastTime = nowTime;
       }
       lastTime = Integer.MAX_VALUE;
       for (Edge e : (Iterable<TitanEdge>)u.query().labels("connectDesc").direction(OUT).limit(20).edges()) {
           int nowTime = e.value("time");
           assertTrue(lastTime + " vs. " + nowTime, lastTime >= nowTime);
           lastTime = nowTime;
       }
       assertEquals(10, size(v.query().labels("connect").direction(OUT).has("time", Cmp.GREATER_THAN, 60).limit(10).vertices()));
       assertEquals(10, size(u.query().labels("connectDesc").direction(OUT).has("time", Cmp.GREATER_THAN, 60).limit(10).vertices()));

       outer = v.query().labels("connect").direction(OUT).limit(20).edges().iterator();
       for (Edge e : (Iterable<TitanEdge>)v.query().labels("connect").direction(OUT).limit(10).edges()) {
           assertEquals(e, outer.next());
       }

       evaluateQuery(v.query().labels("connect").direction(OUT).interval("time", 3, 31),EDGE,10,1,new boolean[]{true,true});
       evaluateQuery(v.query().labels("connect").direction(OUT).has("time", 15).has("weight", 3.5),EDGE,1,1,new boolean[]{false,true});
       evaluateQuery(u.query().labels("connectDesc").direction(OUT).interval("time", 3, 31),EDGE,10,1,new boolean[]{true,true});
       assertEquals(10, v.query().labels("connect").direction(IN).interval("time", 3, 31).count());
       assertEquals(10, u.query().labels("connectDesc").direction(IN).interval("time", 3, 31).count());
       assertEquals(0, v.query().labels("connect").direction(OUT).has("time", null).count());
       assertEquals(10, v.query().labels("connect").direction(OUT).interval("time", 3, 31).vertexIds().size());
       assertEquals(edgesPerLabel-10, v.query().labels("connect").direction(OUT).has("time", Cmp.GREATER_THAN, 31).count());
       assertEquals(10, size(v.query().labels("connect").direction(OUT).interval("time", 3, 31).vertices()));
       assertEquals(3, v.query().labels("friend").direction(OUT).limit(3).count());
       evaluateQuery(v.query().labels("friend").direction(OUT).has("weight", 0.5).limit(3), EDGE, 3, 1, new boolean[]{true, true});
       evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 3, 33).has("weight", 0.5),EDGE,3,1,new boolean[]{true,true});
       evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 3, 33).has("weight", Contain.IN, ImmutableList.of(0.5)),EDGE,3,1,new boolean[]{true,true});
       evaluateQuery(v.query().labels("friend").direction(OUT).has("weight", Contain.IN, ImmutableList.of(0.5,1.5,2.5)).interval("time", 3, 33),EDGE,7,3,new boolean[]{true,true});
       evaluateQuery(v.query().labels("friend").direction(OUT).has("weight", Contain.IN, ImmutableList.of(0.5,1.5)),EDGE,1667,2,new boolean[]{true,true});
       assertEquals(3, u.query().labels("friendDesc").direction(OUT).interval("time", 3, 33).has("weight", 0.5).count());
       assertEquals(1, v.query().labels("friend").direction(OUT).has("weight", 0.5).interval("time", 4, 10).count());
       assertEquals(1, u.query().labels("friendDesc").direction(OUT).has("weight", 0.5).interval("time", 4, 10).count());
       assertEquals(3, v.query().labels("friend").direction(OUT).interval("time", 3, 33).has("weight", 0.5).count());
       assertEquals(4, v.query().labels("friend").direction(OUT).has("time", Cmp.LESS_THAN_EQUAL, 10).count());
       assertEquals(edgesPerLabel-4, v.query().labels("friend").direction(OUT).has("time", Cmp.GREATER_THAN, 10).count());
       assertEquals(20, v.query().labels("friend", "connect").direction(OUT).interval("time", 3, 33).count());

       assertEquals((int)Math.ceil(edgesPerLabel/5.0), v.query().labels("knows").direction(OUT).has("author", v).count());
       assertEquals((int)Math.ceil(edgesPerLabel/5.0), v.query().labels("knows").direction(OUT).has("author", v).interval("weight", 0.0, 4.0).count());
       assertEquals((int)Math.ceil(edgesPerLabel/(5.0*2)), v.query().labels("knows").direction(OUT).has("author", v).interval("weight", 0.0, 2.0).count());
       assertEquals((int)Math.floor(edgesPerLabel/(5.0*2)), v.query().labels("knows").direction(OUT).has("author", v).interval("weight", 2.1, 4.0).count());
       assertEquals(20, size(v.query().labels("connect", "friend").direction(OUT).interval("time", 3, 33).vertices()));
       assertEquals(20, size(v.query().labels("connect", "friend").direction(OUT).interval("time", 3, 33).vertexIds()));
       assertEquals(30, v.query().labels("friend", "connect", "knows").direction(OUT).interval("time", 3, 33).count());
       assertEquals(noVertices-2, v.query().labels("friend", "connect", "knows").direction(OUT).has("time", Cmp.NOT_EQUAL, 10).count());

       assertEquals(0, v.query().has("age", null).labels("undefined").direction(OUT).count());
       assertEquals(1, v.query().labels("connect").direction(OUT).adjacent(vs[6]).has("time", 6).count());
       assertEquals(1, v.query().labels("knows").direction(OUT).adjacent(vs[11]).count());
       assertEquals(1, v.query().labels("knows").direction(IN).adjacent(vs[11]).count());
       assertEquals(2, v.query().labels("knows").direction(BOTH).adjacent(vs[11]).count());
       assertEquals(1, v.query().labels("knows").direction(OUT).adjacent(vs[11]).has("weight", 3.5).count());
       assertEquals(2, v.query().labels("connect").adjacent(vs[6]).has("time", 6).count());
       assertEquals(0, v.query().labels("connect").adjacent(vs[8]).has("time", 8).count());

       assertEquals(edgesPerLabel, v.query().labels("connect").direction(OUT).count());
       assertEquals(edgesPerLabel, v.query().labels("connect").direction(IN).count());
       assertEquals(2*edgesPerLabel, v.query().labels("connect").direction(BOTH).count());

       assertEquals(edgesPerLabel, v.query().labels("connect").has("undefined", null).direction(OUT).count());
       assertEquals(2 * (int) Math.ceil((noVertices - 1) / 4.0), size(v.query().labels("connect", "friend", "knows").has("weight", 1.5).vertexIds()));
       assertEquals(1, v.query().direction(IN).has("time", 1).count());
       assertEquals(10, v.query().direction(OUT).interval("time", 4, 14).count());
       assertEquals(9, v.query().direction(IN).interval("time", 4, 14).has("time", Cmp.NOT_EQUAL, 10).count());
       assertEquals(9, v.query().direction(OUT).interval("time", 4, 14).has("time", Cmp.NOT_EQUAL, 10).count());
       assertEquals(noVertices - 1, size(v.query().direction(OUT).vertices()));
       assertEquals(noVertices - 1, size(v.query().direction(IN).vertices()));
       for (Direction dir : new Direction[]{IN,OUT}) {
           vl = v.query().labels().direction(dir).interval("time", 3, 31).vertexIds();
           vl.sort();
           for (int i = 0; i < vl.size(); i++) assertEquals(vidsubset[i], vl.getID(i));
       }
       assertCount(2*(noVertices-1), v.query().direction(Direction.BOTH).edges());


       //Property queries
       assertEquals(1, size(v.query().properties()));
       assertEquals(1, size(v.query().keys("name").properties()));

       //MultiQueries
       results = tx.multiQuery(qvs).direction(IN).labels("connect").edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(1, size(result));
       results = tx.multiQuery(Sets.newHashSet(qvs)).labels("connect").edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(2, size(result));
       results = tx.multiQuery(qvs).labels("knows").edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(0, size(result));
       results = tx.multiQuery(qvs).edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(4, size(result));
       results2 = tx.multiQuery(qvs).properties();
       for (Iterable<TitanVertexProperty> result : results2.values()) assertEquals(1, size(result));
       results2 = tx.multiQuery(qvs).keys("name").properties();
       for (Iterable<TitanVertexProperty> result : results2.values()) assertEquals(1, size(result));

       //##################################################
       //Same queries as above but without memory loading (i.e. omitting the first query)
       //##################################################
       clopen();
       for (int i = 1; i < noVertices; i++) vs[i] = getV(tx,vs[i].longId());
       v = getV(tx,v.longId());
       u = getV(tx,u.longId());
       qvs = new TitanVertex[]{vs[6], vs[9], vs[12], vs[15], vs[60]};

       assertEquals(10, size(v.query().labels("connect").limit(10).vertices()));
       assertEquals(10, size(u.query().labels("connectDesc").limit(10).vertices()));
       assertEquals(10, size(v.query().labels("connect").has("time", Cmp.GREATER_THAN, 30).limit(10).vertices()));
       assertEquals(10, size(u.query().labels("connectDesc").has("time", Cmp.GREATER_THAN, 30).limit(10).vertices()));

       lastTime = 0;
       for (Edge e : (Iterable<TitanEdge>)v.query().labels("connect").direction(OUT).limit(20).edges()) {
           int nowTime = e.value("time");
           assertTrue(lastTime + " vs. " + nowTime, lastTime <= nowTime);
           lastTime = nowTime;
       }
       lastTime = Integer.MAX_VALUE;
       for (Edge e : (Iterable<TitanEdge>)u.query().labels("connectDesc").direction(OUT).limit(20).edges()) {
           int nowTime = e.value("time");
           assertTrue(lastTime + " vs. " + nowTime, lastTime >= nowTime);
           lastTime = nowTime;
       }
       assertEquals(10, size(v.query().labels("connect").direction(OUT).has("time", Cmp.GREATER_THAN, 60).limit(10).vertices()));
       assertEquals(10, size(u.query().labels("connectDesc").direction(OUT).has("time", Cmp.GREATER_THAN, 60).limit(10).vertices()));

       outer = v.query().labels("connect").direction(OUT).limit(20).edges().iterator();
       for (Edge e : (Iterable<TitanEdge>)v.query().labels("connect").direction(OUT).limit(10).edges()) {
           assertEquals(e, outer.next());
       }

       evaluateQuery(v.query().labels("connect").direction(OUT).interval("time", 3, 31),EDGE,10,1,new boolean[]{true,true});
       evaluateQuery(v.query().labels("connect").direction(OUT).has("time", 15).has("weight", 3.5),EDGE,1,1,new boolean[]{false,true});
       evaluateQuery(u.query().labels("connectDesc").direction(OUT).interval("time", 3, 31),EDGE,10,1,new boolean[]{true,true});
       assertEquals(10, v.query().labels("connect").direction(IN).interval("time", 3, 31).count());
       assertEquals(10, u.query().labels("connectDesc").direction(IN).interval("time", 3, 31).count());
       assertEquals(0, v.query().labels("connect").direction(OUT).has("time", null).count());
       assertEquals(10, v.query().labels("connect").direction(OUT).interval("time", 3, 31).vertexIds().size());
       assertEquals(edgesPerLabel-10, v.query().labels("connect").direction(OUT).has("time", Cmp.GREATER_THAN, 31).count());
       assertEquals(10, size(v.query().labels("connect").direction(OUT).interval("time", 3, 31).vertices()));
       assertEquals(3, v.query().labels("friend").direction(OUT).limit(3).count());
       evaluateQuery(v.query().labels("friend").direction(OUT).has("weight", 0.5).limit(3), EDGE, 3, 1, new boolean[]{true, true});
       evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 3, 33).has("weight", 0.5),EDGE,3,1,new boolean[]{true,true});
       evaluateQuery(v.query().labels("friend").direction(OUT).interval("time", 3, 33).has("weight", Contain.IN, ImmutableList.of(0.5)),EDGE,3,1,new boolean[]{true,true});
       evaluateQuery(v.query().labels("friend").direction(OUT).has("weight", Contain.IN, ImmutableList.of(0.5,1.5,2.5)).interval("time", 3, 33),EDGE,7,3,new boolean[]{true,true});
       evaluateQuery(v.query().labels("friend").direction(OUT).has("weight", Contain.IN, ImmutableList.of(0.5,1.5)),EDGE,1667,2,new boolean[]{true,true});
       assertEquals(3, u.query().labels("friendDesc").direction(OUT).interval("time", 3, 33).has("weight", 0.5).count());
       assertEquals(1, v.query().labels("friend").direction(OUT).has("weight", 0.5).interval("time", 4, 10).count());
       assertEquals(1, u.query().labels("friendDesc").direction(OUT).has("weight", 0.5).interval("time", 4, 10).count());
       assertEquals(3, v.query().labels("friend").direction(OUT).interval("time", 3, 33).has("weight", 0.5).count());
       assertEquals(4, v.query().labels("friend").direction(OUT).has("time", Cmp.LESS_THAN_EQUAL, 10).count());
       assertEquals(edgesPerLabel-4, v.query().labels("friend").direction(OUT).has("time", Cmp.GREATER_THAN, 10).count());
       assertEquals(20, v.query().labels("friend", "connect").direction(OUT).interval("time", 3, 33).count());

       assertEquals((int)Math.ceil(edgesPerLabel/5.0), v.query().labels("knows").direction(OUT).has("author", v).count());
       assertEquals((int)Math.ceil(edgesPerLabel/5.0), v.query().labels("knows").direction(OUT).has("author", v).interval("weight", 0.0, 4.0).count());
       assertEquals((int)Math.ceil(edgesPerLabel/(5.0*2)), v.query().labels("knows").direction(OUT).has("author", v).interval("weight", 0.0, 2.0).count());
       assertEquals((int)Math.floor(edgesPerLabel/(5.0*2)), v.query().labels("knows").direction(OUT).has("author", v).interval("weight", 2.1, 4.0).count());
       assertEquals(20, size(v.query().labels("connect", "friend").direction(OUT).interval("time", 3, 33).vertices()));
       assertEquals(20, size(v.query().labels("connect", "friend").direction(OUT).interval("time", 3, 33).vertexIds()));
       assertEquals(30, v.query().labels("friend", "connect", "knows").direction(OUT).interval("time", 3, 33).count());
       assertEquals(noVertices-2, v.query().labels("friend", "connect", "knows").direction(OUT).has("time", Cmp.NOT_EQUAL, 10).count());

       assertEquals(0, v.query().has("age", null).labels("undefined").direction(OUT).count());
       assertEquals(1, v.query().labels("connect").direction(OUT).adjacent(vs[6]).has("time", 6).count());
       assertEquals(1, v.query().labels("knows").direction(OUT).adjacent(vs[11]).count());
       assertEquals(1, v.query().labels("knows").direction(IN).adjacent(vs[11]).count());
       assertEquals(2, v.query().labels("knows").direction(BOTH).adjacent(vs[11]).count());
       assertEquals(1, v.query().labels("knows").direction(OUT).adjacent(vs[11]).has("weight", 3.5).count());
       assertEquals(2, v.query().labels("connect").adjacent(vs[6]).has("time", 6).count());
       assertEquals(0, v.query().labels("connect").adjacent(vs[8]).has("time", 8).count());

       assertEquals(edgesPerLabel, v.query().labels("connect").direction(OUT).count());
       assertEquals(edgesPerLabel, v.query().labels("connect").direction(IN).count());
       assertEquals(2*edgesPerLabel, v.query().labels("connect").direction(BOTH).count());

       assertEquals(edgesPerLabel, v.query().labels("connect").has("undefined", null).direction(OUT).count());
       assertEquals(2*(int)Math.ceil((noVertices-1)/4.0), size(v.query().labels("connect", "friend", "knows").has("weight", 1.5).vertexIds()));
       assertEquals(1, v.query().direction(IN).has("time", 1).count());
       assertEquals(10, v.query().direction(OUT).interval("time", 4, 14).count());
       assertEquals(9, v.query().direction(IN).interval("time", 4, 14).has("time", Cmp.NOT_EQUAL, 10).count());
       assertEquals(9, v.query().direction(OUT).interval("time", 4, 14).has("time", Cmp.NOT_EQUAL, 10).count());
       assertEquals(noVertices-1, size(v.query().direction(OUT).vertices()));
       assertEquals(noVertices-1, size(v.query().direction(IN).vertices()));
       for (Direction dir : new Direction[]{IN,OUT}) {
           vl = v.query().labels().direction(dir).interval("time", 3, 31).vertexIds();
           vl.sort();
           for (int i = 0; i < vl.size(); i++) assertEquals(vidsubset[i], vl.getID(i));
       }
       assertCount(2 * (noVertices - 1), v.query().direction(Direction.BOTH).edges());


       //Property queries
       assertEquals(1, size(v.query().properties()));
       assertEquals(1, size(v.query().keys("name").properties()));

       //MultiQueries
       results = tx.multiQuery(qvs).direction(IN).labels("connect").edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(1, size(result));
       results = tx.multiQuery(Sets.newHashSet(qvs)).labels("connect").edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(2, size(result));
       results = tx.multiQuery(qvs).labels("knows").edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(0, size(result));
       results = tx.multiQuery(qvs).edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(4, size(result));
       results2 = tx.multiQuery(qvs).properties();
       for (Iterable<TitanVertexProperty> result : results2.values()) assertEquals(1, size(result));
       results2 = tx.multiQuery(qvs).keys("name").properties();
       for (Iterable<TitanVertexProperty> result : results2.values()) assertEquals(1, size(result));

       //##################################################
       //End copied queries
       //##################################################

       newTx();

       v = getOnlyElement(tx.query().has("name", "v").vertices());
       assertNotNull(v);
       assertEquals(2, v.query().has("weight", 1.5).interval("time", 10, 30).limit(2).vertexIds().size());
       assertEquals(10, v.query().has("weight", 1.5).interval("time", 10, 30).vertexIds().size());

       newTx();

       v = getOnlyElement(tx.query().has("name", "v").vertices());
       assertNotNull(v);
       assertEquals(2, v.query().has("weight", 1.5).interval("time", 10, 30).limit(2).count());
       assertEquals(10, v.query().has("weight", 1.5).interval("time", 10, 30).count());


       newTx();
       //Test partially new vertex queries
       TitanVertex[] qvs2 = new TitanVertex[qvs.length+2];
       qvs2[0]=tx.addVertex();
       for (int i=0;i<qvs.length;i++) qvs2[i+1]=getV(tx,qvs[i].longId());
       qvs2[qvs2.length-1]=tx.addVertex();
       qvs2[0].addEdge("connect",qvs2[qvs2.length-1]);
       qvs2[qvs2.length-1].addEdge("connect", qvs2[0]);
       results = tx.multiQuery(qvs2).direction(IN).labels("connect").edges();
       for (Iterable<TitanEdge> result : results.values()) assertEquals(1, size(result));

   }

    @Test
    public void testRelationTypeIndexes() {
        PropertyKey weight = makeKey("weight",Decimal.class);
        PropertyKey time = makeKey("time",Long.class);

        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.LIST).make();
        EdgeLabel connect = mgmt.makeEdgeLabel("connect").signature(time).make();
        EdgeLabel child = mgmt.makeEdgeLabel("child").multiplicity(Multiplicity.ONE2MANY).make();
        EdgeLabel link = mgmt.makeEdgeLabel("link").unidirected().make();

        RelationTypeIndex name1 = mgmt.buildPropertyIndex(name, "weightDesc", weight);

        RelationTypeIndex connect1 = mgmt.buildEdgeIndex(connect, "weightAsc", Direction.BOTH, incr, weight);
        RelationTypeIndex connect2 = mgmt.buildEdgeIndex(connect, "weightDesc", Direction.OUT, decr, weight);
        RelationTypeIndex connect3 = mgmt.buildEdgeIndex(connect, "time+weight", Direction.OUT, decr, time, weight);

        RelationTypeIndex child1 = mgmt.buildEdgeIndex(child, "time", Direction.OUT, time);

        RelationTypeIndex link1 = mgmt.buildEdgeIndex(link, "time", Direction.OUT, time);

        final String name1n = name1.name(), connect1n = connect1.name(), connect2n = connect2.name(),
                connect3n = connect3.name(), child1n = child1.name(), link1n = link1.name();

        // ########### INSPECTION & FAILURE ##############

        assertTrue(mgmt.containsRelationIndex(name,"weightDesc"));
        assertTrue(mgmt.containsRelationIndex(connect,"weightDesc"));
        assertFalse(mgmt.containsRelationIndex(child,"weightDesc"));
        assertEquals("time+weight",mgmt.getRelationIndex(connect,"time+weight").name());
        assertNotNull(mgmt.getRelationIndex(link,"time"));
        assertNull(mgmt.getRelationIndex(name,"time"));
        assertEquals(1, size(mgmt.getRelationIndexes(child)));
        assertEquals(3, size(mgmt.getRelationIndexes(connect)));
        assertEquals(0, size(mgmt.getRelationIndexes(weight)));
        try {
           //Name already exists
           mgmt.buildEdgeIndex(connect, "weightAsc", Direction.OUT, time);
           fail();
        } catch (SchemaViolationException e) {}
//        try {
//           //Invalid key - must be single valued
//           mgmt.createEdgeIndex(connect,"blablub",Direction.OUT,name);
//           fail();
//        } catch (IllegalArgumentException e) {}
        try {
           //Not valid in this direction due to multiplicity constraint
           mgmt.buildEdgeIndex(child, "blablub", Direction.IN, time);
           fail();
        } catch (IllegalArgumentException e) {}
        try {
           //Not valid in this direction due to unidirectionality
           mgmt.buildEdgeIndex(link, "blablub", Direction.BOTH, time);
           fail();
        } catch (IllegalArgumentException e) {}

        // ########## END INSPECTION ###########

        finishSchema();

        weight = mgmt.getPropertyKey("weight");
        time = mgmt.getPropertyKey("time");

        name = mgmt.getPropertyKey("name");
        connect = mgmt.getEdgeLabel("connect");
        child = mgmt.getEdgeLabel("child");
        link = mgmt.getEdgeLabel("link");

        // ########### INSPECTION & FAILURE (copied from above) ##############

        assertTrue(mgmt.containsRelationIndex(name,"weightDesc"));
        assertTrue(mgmt.containsRelationIndex(connect,"weightDesc"));
        assertFalse(mgmt.containsRelationIndex(child, "weightDesc"));
        assertEquals("time+weight",mgmt.getRelationIndex(connect,"time+weight").name());
        assertNotNull(mgmt.getRelationIndex(link, "time"));
        assertNull(mgmt.getRelationIndex(name, "time"));
        assertEquals(1, Iterables.size(mgmt.getRelationIndexes(child)));
        assertEquals(3,Iterables.size(mgmt.getRelationIndexes(connect)));
        assertEquals(0,Iterables.size(mgmt.getRelationIndexes(weight)));
        try {
           //Name already exists
           mgmt.buildEdgeIndex(connect, "weightAsc", Direction.OUT, time);
           fail();
        } catch (SchemaViolationException e) {}
//        try {
//           //Invalid key - must be single valued
//           mgmt.createEdgeIndex(connect,"blablub",Direction.OUT,name);
//           fail();
//        } catch (IllegalArgumentException e) {}
        try {
           //Not valid in this direction due to multiplicity constraint
           mgmt.buildEdgeIndex(child, "blablub", Direction.IN, time);
           fail();
        } catch (IllegalArgumentException e) {}
        try {
           //Not valid in this direction due to unidirectionality
           mgmt.buildEdgeIndex(link, "blablub", Direction.BOTH, time);
           fail();
        } catch (IllegalArgumentException e) {}

        // ########## END INSPECTION ###########

        mgmt.rollback();

        /*
        ########## TEST WITHIN TRANSACTION ##################
        */

        weight = tx.getPropertyKey("weight");
        time = tx.getPropertyKey("time");

        final int numV = 100;
        TitanVertex v = tx.addVertex();
        TitanVertex ns[] = new TitanVertex[numV];

        for (int i=0;i<numV;i++) {
            double w = (i*0.5)%5;
            long t = (i+77)%numV;
            VertexProperty p = v.property("name", "v" + i, "weight", w, "time", t);

            ns[i]=tx.addVertex();
            for (String label : new String[]{"connect","child","link"}) {
                Edge e = v.addEdge(label,ns[i],"weight",w,"time",t);
            }
        }
        TitanVertex u = ns[0];
        VertexList vl;

        //######### QUERIES ##########
        v = getV(tx,v);
        u = getV(tx,u);

        evaluateQuery(v.query().keys("name").has("weight",Cmp.GREATER_THAN,3.6),
                PROPERTY, 2*numV/10, 1, new boolean[]{false,true});
        evaluateQuery(v.query().keys("name").has("weight",Cmp.LESS_THAN,0.9).orderBy("weight", incr),
                PROPERTY, 2*numV/10, 1, new boolean[]{true,true},weight,Order.ASC);
        evaluateQuery(v.query().keys("name").interval("weight", 1.1, 2.2).orderBy("weight", decr).limit(numV/10),
                PROPERTY, numV/10, 1, new boolean[]{true,false},weight,Order.DESC);
        evaluateQuery(v.query().keys("name").has("time",Cmp.EQUAL,5).orderBy("weight", decr),
                PROPERTY, 1, 1, new boolean[]{false,false},weight,Order.DESC);
        evaluateQuery(v.query().keys("name"),
                PROPERTY, numV, 1, new boolean[]{true,true});

        evaluateQuery(v.query().labels("child").direction(OUT).has("time",Cmp.EQUAL,5),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("child").direction(BOTH).has("time",Cmp.EQUAL,5),
                EDGE, 1, 2 , new boolean[0]);
        evaluateQuery(v.query().labels("child").direction(OUT).interval("time",10,20).orderBy("weight", decr).limit(5),
                EDGE, 5, 1 , new boolean[]{true,false}, weight, Order.DESC);
        evaluateQuery(v.query().labels("child").direction(BOTH).interval("weight", 0.0, 1.0).orderBy("weight", decr),
                EDGE, 2*numV/10, 2 , new boolean[]{false,false}, weight, Order.DESC);
        evaluateQuery(v.query().labels("child").direction(OUT).interval("weight", 0.0, 1.0),
                EDGE, 2*numV/10, 1 , new boolean[]{false,true});
        evaluateQuery(v.query().labels("child").direction(BOTH),
                EDGE, numV, 1 , new boolean[]{true,true});
        vl = v.query().labels("child").direction(BOTH).vertexIds();
        assertEquals(numV,vl.size());
        assertTrue(vl.isSorted());
        assertTrue(isSortedByID(vl));
        evaluateQuery(v.query().labels("child").interval("weight", 0.0, 1.0).direction(OUT),
                EDGE, 2*numV/10, 1 , new boolean[]{false,true});
        vl = v.query().labels("child").interval("weight", 0.0, 1.0).direction(OUT).vertexIds();
        assertEquals(2*numV/10,vl.size());
        assertTrue(vl.isSorted());
        assertTrue(isSortedByID(vl));
        evaluateQuery(v.query().labels("child").interval("time", 70, 80).direction(OUT).orderBy("time",incr),
                EDGE, 10, 1 , new boolean[]{true,true},time,Order.ASC);
        vl = v.query().labels("child").interval("time", 70, 80).direction(OUT).orderBy("time", incr).vertexIds();
        assertEquals(10,vl.size());
        assertFalse(vl.isSorted());
        assertFalse(isSortedByID(vl));
        vl.sort();
        assertTrue(vl.isSorted());
        assertTrue(isSortedByID(vl));

        evaluateQuery(v.query().labels("connect").has("time",Cmp.EQUAL,5).interval("weight",0.0,5.0).direction(OUT),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("connect").has("time",Cmp.EQUAL,5).interval("weight",0.0,5.0).direction(BOTH),
                EDGE, 1, 2 , new boolean[0]);
        evaluateQuery(v.query().labels("connect").interval("time",10,20).interval("weight",0.0,5.0).direction(OUT),
                EDGE, 10, 1 , new boolean[]{false,true});
        evaluateQuery(v.query().labels("connect").direction(OUT).orderBy("weight", incr).limit(10),
                EDGE, 10, 1 , new boolean[]{true,true},weight,Order.ASC);
        evaluateQuery(v.query().labels("connect").direction(OUT).orderBy("weight",decr).limit(10),
                EDGE, 10, 1 , new boolean[]{true,true},weight,Order.DESC);
        evaluateQuery(v.query().labels("connect").direction(OUT).interval("weight",1.4,2.75).orderBy("weight", decr),
                EDGE, 3*numV/10, 1 , new boolean[]{true,true},weight,Order.DESC);
        evaluateQuery(v.query().labels("connect").direction(OUT).has("time",Cmp.EQUAL,22).orderBy("weight", decr),
                EDGE, 1, 1 , new boolean[]{true,true},weight,Order.DESC);
        evaluateQuery(v.query().labels("connect").direction(OUT).has("time",Cmp.EQUAL,22).orderBy("weight", incr),
                EDGE, 1, 1 , new boolean[]{true,false},weight,Order.ASC);
        evaluateQuery(v.query().labels("connect").direction(OUT).adjacent(u),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("connect").direction(OUT).has("weight", Cmp.EQUAL, 0.0).adjacent(u),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("connect").direction(OUT).interval("weight", 0.0, 1.0).adjacent(u),
                EDGE, 1, 1 , new boolean[]{false,true});
        evaluateQuery(v.query().labels("connect").direction(OUT).interval("time",50,100).adjacent(u),
                EDGE, 1, 1 , new boolean[]{false,true});

        evaluateQuery(v.query(),
                RELATION, numV*4, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().direction(OUT),
                RELATION, numV*4, 1 , new boolean[]{false,true});

        //--------------

        clopen();

        weight = tx.getPropertyKey("weight");
        time = tx.getPropertyKey("time");

        //######### QUERIES (copied from above) ##########
        v = getV(tx,v);
        u = getV(tx,u);

        evaluateQuery(v.query().keys("name").has("weight",Cmp.GREATER_THAN,3.6),
                PROPERTY, 2*numV/10, 1, new boolean[]{false,true});
        evaluateQuery(v.query().keys("name").has("weight",Cmp.LESS_THAN,0.9).orderBy("weight", incr),
                PROPERTY, 2*numV/10, 1, new boolean[]{true,true},weight,Order.ASC);
        evaluateQuery(v.query().keys("name").interval("weight", 1.1, 2.2).orderBy("weight", decr).limit(numV/10),
                PROPERTY, numV/10, 1, new boolean[]{true,false},weight,Order.DESC);
        evaluateQuery(v.query().keys("name").has("time",Cmp.EQUAL,5).orderBy("weight", decr),
                PROPERTY, 1, 1, new boolean[]{false,false},weight,Order.DESC);
        evaluateQuery(v.query().keys("name"),
                PROPERTY, numV, 1, new boolean[]{true,true});

        evaluateQuery(v.query().labels("child").direction(OUT).has("time",Cmp.EQUAL,5),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("child").direction(BOTH).has("time",Cmp.EQUAL,5),
                EDGE, 1, 2 , new boolean[0]);
        evaluateQuery(v.query().labels("child").direction(OUT).interval("time",10,20).orderBy("weight", decr).limit(5),
                EDGE, 5, 1 , new boolean[]{true,false}, weight, Order.DESC);
        evaluateQuery(v.query().labels("child").direction(BOTH).interval("weight", 0.0, 1.0).orderBy("weight", decr),
                EDGE, 2*numV/10, 2 , new boolean[]{false,false}, weight, Order.DESC);
        evaluateQuery(v.query().labels("child").direction(OUT).interval("weight", 0.0, 1.0),
                EDGE, 2*numV/10, 1 , new boolean[]{false,true});
        evaluateQuery(v.query().labels("child").direction(BOTH),
                EDGE, numV, 1 , new boolean[]{true,true});
        vl = v.query().labels("child").direction(BOTH).vertexIds();
        assertEquals(numV,vl.size());
        assertTrue(vl.isSorted());
        assertTrue(isSortedByID(vl));
        evaluateQuery(v.query().labels("child").interval("weight", 0.0, 1.0).direction(OUT),
                EDGE, 2*numV/10, 1 , new boolean[]{false,true});
        vl = v.query().labels("child").interval("weight", 0.0, 1.0).direction(OUT).vertexIds();
        assertEquals(2*numV/10,vl.size());
        assertTrue(vl.isSorted());
        assertTrue(isSortedByID(vl));
        evaluateQuery(v.query().labels("child").interval("time", 70, 80).direction(OUT).orderBy("time",incr),
                EDGE, 10, 1 , new boolean[]{true,true},time,Order.ASC);
        vl = v.query().labels("child").interval("time", 70, 80).direction(OUT).orderBy("time", incr).vertexIds();
        assertEquals(10,vl.size());
        assertFalse(vl.isSorted());
        assertFalse(isSortedByID(vl));
        vl.sort();
        assertTrue(vl.isSorted());
        assertTrue(isSortedByID(vl));

        evaluateQuery(v.query().labels("connect").has("time",Cmp.EQUAL,5).interval("weight",0.0,5.0).direction(OUT),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("connect").has("time",Cmp.EQUAL,5).interval("weight",0.0,5.0).direction(BOTH),
                EDGE, 1, 2 , new boolean[0]);
        evaluateQuery(v.query().labels("connect").interval("time",10,20).interval("weight",0.0,5.0).direction(OUT),
                EDGE, 10, 1 , new boolean[]{false,true});
        evaluateQuery(v.query().labels("connect").direction(OUT).orderBy("weight", incr).limit(10),
                EDGE, 10, 1 , new boolean[]{true,true},weight,Order.ASC);
        evaluateQuery(v.query().labels("connect").direction(OUT).orderBy("weight",decr).limit(10),
                EDGE, 10, 1 , new boolean[]{true,true},weight,Order.DESC);
        evaluateQuery(v.query().labels("connect").direction(OUT).interval("weight",1.4,2.75).orderBy("weight", decr),
                EDGE, 3*numV/10, 1 , new boolean[]{true,true},weight,Order.DESC);
        evaluateQuery(v.query().labels("connect").direction(OUT).has("time",Cmp.EQUAL,22).orderBy("weight", decr),
                EDGE, 1, 1 , new boolean[]{true,true},weight,Order.DESC);
        evaluateQuery(v.query().labels("connect").direction(OUT).has("time",Cmp.EQUAL,22).orderBy("weight", incr),
                EDGE, 1, 1 , new boolean[]{true,false},weight,Order.ASC);
        evaluateQuery(v.query().labels("connect").direction(OUT).adjacent(u),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("connect").direction(OUT).has("weight", Cmp.EQUAL, 0.0).adjacent(u),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("connect").direction(OUT).interval("weight", 0.0, 1.0).adjacent(u),
                EDGE, 1, 1 , new boolean[]{false,true});
        evaluateQuery(v.query().labels("connect").direction(OUT).interval("time",50,100).adjacent(u),
                EDGE, 1, 1 , new boolean[]{false,true});

        evaluateQuery(v.query(),
                RELATION, numV*4, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().direction(OUT),
                RELATION, numV*4, 1 , new boolean[]{false,true});

        //--------------

        //Update in transaction
        for (TitanVertexProperty<String> p : v.query().labels("name").properties()) {
            if (p.<Long>value("time")<(numV/2)) p.remove();
        }
        for (TitanEdge e : v.query().direction(BOTH).edges()) {
            if (e.<Long>value("time")<(numV/2)) e.remove();
        }
        ns = new TitanVertex[numV*3/2];
        for (int i=numV;i<numV*3/2;i++) {
            double w = (i*0.5)%5;
            long t = i;
            v.property("name","v"+i,"weight",w,"time",t);

            ns[i]=tx.addVertex();
            for (String label : new String[]{"connect","child","link"}) {
                TitanEdge e = v.addEdge(label,ns[i],"weight",w, "time",t);
            }
        }

        //######### UPDATED QUERIES ##########

        evaluateQuery(v.query().keys("name").has("weight",Cmp.GREATER_THAN,3.6),
                PROPERTY, 2*numV/10, 1, new boolean[]{false,true});
        evaluateQuery(v.query().keys("name").interval("time",numV/2-10,numV/2+10),
                PROPERTY, 10, 1, new boolean[]{false,true});
        evaluateQuery(v.query().keys("name").interval("time",numV/2-10,numV/2+10).orderBy("weight", decr),
                PROPERTY, 10, 1, new boolean[]{false,false},weight,Order.DESC);
        evaluateQuery(v.query().keys("name").interval("time",numV,numV+10).limit(5),
                PROPERTY, 5, 1, new boolean[]{false,true});

        evaluateQuery(v.query().labels("child").direction(OUT).has("time",Cmp.EQUAL,5),
                EDGE, 0, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("child").direction(OUT).has("time",Cmp.EQUAL,numV+5),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("child").direction(OUT).interval("time",10,20).orderBy("weight", decr).limit(5),
                EDGE, 0, 1 , new boolean[]{true,false}, weight, Order.DESC);
        evaluateQuery(v.query().labels("child").direction(OUT).interval("time",numV+10,numV+20).orderBy("weight", decr).limit(5),
                EDGE, 5, 1 , new boolean[]{true,false}, weight, Order.DESC);


        evaluateQuery(v.query(),
                RELATION, numV*4, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().direction(OUT),
                RELATION, numV*4, 1 , new boolean[]{false,true});

        //######### END UPDATED QUERIES ##########

        newTx();

        weight = tx.getPropertyKey("weight");
        time = tx.getPropertyKey("time");

        v = getV(tx,v);
        u = getV(tx,u);

        //######### UPDATED QUERIES (copied from above) ##########

        evaluateQuery(v.query().keys("name").has("weight",Cmp.GREATER_THAN,3.6),
                PROPERTY, 2*numV/10, 1, new boolean[]{false,true});
        evaluateQuery(v.query().keys("name").interval("time",numV/2-10,numV/2+10),
                PROPERTY, 10, 1, new boolean[]{false,true});
        evaluateQuery(v.query().keys("name").interval("time",numV/2-10,numV/2+10).orderBy("weight", decr),
                PROPERTY, 10, 1, new boolean[]{false,false},weight,Order.DESC);
        evaluateQuery(v.query().keys("name").interval("time",numV,numV+10).limit(5),
                PROPERTY, 5, 1, new boolean[]{false,true});

        evaluateQuery(v.query().labels("child").direction(OUT).has("time",Cmp.EQUAL,5),
                EDGE, 0, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("child").direction(OUT).has("time",Cmp.EQUAL,numV+5),
                EDGE, 1, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().labels("child").direction(OUT).interval("time",10,20).orderBy("weight", decr).limit(5),
                EDGE, 0, 1 , new boolean[]{true,false}, weight, Order.DESC);
        evaluateQuery(v.query().labels("child").direction(OUT).interval("time",numV+10,numV+20).orderBy("weight", decr).limit(5),
                EDGE, 5, 1 , new boolean[]{true,false}, weight, Order.DESC);


        evaluateQuery(v.query(),
                RELATION, numV*4, 1 , new boolean[]{true,true});
        evaluateQuery(v.query().direction(OUT),
                RELATION, numV*4, 1 , new boolean[]{false,true});

        //######### END UPDATED QUERIES ##########

    }

    private boolean isSortedByID(VertexList vl) {
        for (int i=1;i<vl.size();i++) {
            if (vl.getID(i-1)>vl.getID(i)) return false;
        }
        return true;
    }

    public static void evaluateQuery(TitanVertexQuery query, RelationCategory resultType,
                               int expectedResults, int numSubQueries, boolean[] subQuerySpecs) {
        evaluateQuery(query,resultType,expectedResults,numSubQueries, subQuerySpecs, ImmutableMap.<PropertyKey,Order>of());
    }

    public static void evaluateQuery(TitanVertexQuery query, RelationCategory resultType,
                               int expectedResults, int numSubQueries, boolean[] subQuerySpecs,
                               PropertyKey orderKey, Order order) {
        evaluateQuery(query,resultType,expectedResults,numSubQueries, subQuerySpecs, ImmutableMap.of(orderKey,order));
    }


    public static void evaluateQuery(TitanVertexQuery query, RelationCategory resultType,
                               int expectedResults, int numSubQueries, boolean[] subQuerySpecs,
                               Map<PropertyKey,Order> orderMap) {
        QueryDescription qd;
        switch(resultType) {
            case PROPERTY: qd = query.describeForProperties(); break;
            case EDGE: qd = query.describeForEdges(); break;
            case RELATION: qd = ((BasicVertexCentricQueryBuilder)query).describeForRelations(); break;
            default: throw new AssertionError();
        }
        assertEquals(1,qd.getNoCombinedQueries());
        assertEquals(numSubQueries,qd.getNoSubQueries());
        List<? extends QueryDescription.SubQuery> subqs = qd.getSubQueries();
        assertEquals(numSubQueries,subqs.size());
        for (int i=0;i<numSubQueries;i++) {
            QueryDescription.SubQuery sq = subqs.get(i);
            assertNotNull(sq);
            if (subQuerySpecs.length==2) { //0=>fitted, 1=>ordered
                assertEquals(subQuerySpecs[0],sq.isFitted());
                assertEquals(subQuerySpecs[1],sq.isSorted());
            }
            assertEquals(1,((StandardQueryDescription.StandardSubQuery)sq).numIntersectingQueries());
        }
        //Check order
        OrderList orders = ((StandardQueryDescription)qd).getQueryOrder();
        assertNotNull(orders);
        assertEquals(orderMap.size(),orders.size());
        for (int i=0;i<orders.size();i++) {
            assertEquals(orderMap.get(orders.getKey(i)),orders.getOrder(i));
        }
        for (PropertyKey key : orderMap.keySet()) assertTrue(orders.containsKey(key));

        Iterable<? extends TitanElement> result;
        switch(resultType) {
            case PROPERTY: result = query.properties(); break;
            case EDGE: result = query.edges(); break;
            case RELATION: result = query.relations(); break;
            default: throw new AssertionError();
        }
        int no = 0;
        TitanElement previous = null;
        for (TitanElement e : result) {
            assertNotNull(e);
            no++;
            if (previous!=null && !orders.isEmpty()) {
                assertTrue(orders.compare(previous,e)<=0);
            }
            previous = e;
        }
        assertEquals(expectedResults,no);
    }

    @Test
    public void testEdgesExceedCacheSize() {
        // Add a vertex with as many edges as the tx-cache-size. (20000 by default)
        int numEdges = graph.getConfiguration().getTxVertexCacheSize();
        TitanVertex parentVertex = graph.addVertex();
        for (int i = 0; i < numEdges; i++) {
            TitanVertex childVertex = graph.addVertex();
            parentVertex.addEdge("friend", childVertex);
        }
        graph.tx().commit();
        assertCount(numEdges, parentVertex.query().direction(Direction.OUT).edges());

        // Remove an edge.
        getOnlyElement(parentVertex.query().direction(OUT).edges()).remove();

        // Check that getEdges returns one fewer.
        assertCount(numEdges - 1, parentVertex.query().direction(Direction.OUT).edges());

        // Run the same check one more time.
        // This fails! (Expected: 19999. Actual: 20000.)
        assertCount(numEdges - 1, parentVertex.query().direction(Direction.OUT).edges());
    }

    // TODO this can be rewritten without TP3 traversal types, but then much of its unique value is lost
    @Test
    public void testTinkerPopOptimizationStrategies() {
        PropertyKey id = mgmt.makePropertyKey("id").cardinality(Cardinality.SINGLE).dataType(Integer.class).make();
        PropertyKey weight = mgmt.makePropertyKey("weight").cardinality(Cardinality.SINGLE).dataType(Integer.class).make();

        mgmt.buildIndex("byId",Vertex.class).addKey(id).buildCompositeIndex();
        mgmt.buildIndex("byWeight",Vertex.class).addKey(weight).buildCompositeIndex();
        mgmt.buildIndex("byIdWeight",Vertex.class).addKey(id).addKey(weight).buildCompositeIndex();

        EdgeLabel knows = mgmt.makeEdgeLabel("knows").make();
        mgmt.buildEdgeIndex(knows,"byWeightDecr",Direction.OUT,decr,weight);
        mgmt.buildEdgeIndex(knows,"byWeightIncr",Direction.OUT,incr,weight);

        finishSchema();


        int numV = 100;
        TitanVertex[] vs = new TitanVertex[numV];
        for (int i=0;i<numV;i++) {
            vs[i]=graph.addVertex("id",i,"weight",i%5);
        }
        int superV = 10;
        int sid = -1;
        TitanVertex[] sv = new TitanVertex[superV];
        for (int i=0;i<superV;i++) {
            sv[i]=graph.addVertex("id",sid);
            for (int j=0;j<numV;j++) {
                sv[i].addEdge("knows",vs[j],"weight",j%5);
            }
        }

        assertNumStep(numV/5, 1, sv[0].outE("knows").has("weight",1), TitanVertexStep.class);
        assertNumStep(numV, 1, sv[0].outE("knows"), TitanVertexStep.class);
        assertNumStep(numV, 1, sv[0].out("knows"), TitanVertexStep.class);
        assertNumStep(10, 1, sv[0].local(__.outE("knows").limit(10)), TitanVertexStep.class);
        assertNumStep(10, 1, sv[0].local(__.outE("knows").range(10, 20)), LocalStep.class);
        assertNumStep(numV, 2, sv[0].outE("knows").order().by("weight", decr), TitanVertexStep.class, OrderStep.class);
        assertNumStep(10, 1, sv[0].local(__.outE("knows").order().by("weight", decr).limit(10)), TitanVertexStep.class);
        assertNumStep(numV/5, 2, sv[0].outE("knows").has("weight").order().by("weight", incr).has("weight", 1), TitanVertexStep.class, OrderStep.class);
        assertNumStep(10, 1, sv[0].local(__.outE("knows").has("weight").order().by("weight", incr).has("weight", 1).limit(10)), TitanVertexStep.class);
        assertNumStep(5, 1, sv[0].local(__.outE("knows").has("weight").order().by("weight", incr).has("weight", 1).range(10, 15)), LocalStep.class);

        //Global graph queries
        assertNumStep(1, 1, graph.V().has("id", numV / 5), TitanGraphStep.class);
        assertNumStep(1, 1, graph.V().has("id", numV / 5).has("weight", (numV / 5) % 5), TitanGraphStep.class);
        assertNumStep(numV / 5, 1, graph.V().has("weight", 1), TitanGraphStep.class);
        assertNumStep(10, 1, graph.V().has("weight", 1).range(0, 10), TitanGraphStep.class);

        assertNumStep(superV, 1, graph.V().has("id",sid), TitanGraphStep.class);

        assertNumStep(superV*(numV/5), 2, graph.V().has("id", sid).outE("knows").has("weight", 1), TitanGraphStep.class, TitanVertexStep.class);
        assertNumStep(superV*(numV/5*2), 2, graph.V().has("id",sid).outE("knows").between("weight", 1, 3), TitanGraphStep.class, TitanVertexStep.class);
        assertNumStep(superV*10, 2, graph.V().has("id", sid).local(__.outE("knows").between("weight", 1, 3).limit(10)), TitanGraphStep.class, TitanVertexStep.class);
        assertNumStep(superV*10, 2, graph.V().has("id", sid).local(__.outE("knows").between("weight", 1, 3).order().by("weight", decr).limit(10)), TitanGraphStep.class, TitanVertexStep.class);

        clopen(option(USE_MULTIQUERY),true);

        assertNumStep(superV*(numV/5), 2, graph.V().has("id",sid).outE("knows").has("weight",1), TitanGraphStep.class, TitanVertexStep.class);
        assertNumStep(superV*(numV/5*2), 2, graph.V().has("id",sid).outE("knows").between("weight", 1, 3), TitanGraphStep.class, TitanVertexStep.class);
        assertNumStep(superV*10, 2, graph.V().has("id", sid).local(__.outE("knows").between("weight", 1, 3).limit(10)), TitanGraphStep.class, TitanVertexStep.class);
        assertNumStep(superV*10, 2, graph.V().has("id", sid).local(__.outE("knows").between("weight", 1, 3).order().by("weight", decr).limit(10)), TitanGraphStep.class, TitanVertexStep.class);

    }

    private static void assertNumStep(int expectedResults, int expectedSteps, Traversal traversal, Class<? extends Step>... expectedStepTypes) {
        int num = 0;
        while (traversal.hasNext()) {
            traversal.next();
            num++;
        }
        assertEquals(expectedResults,num);

//        traversal.getStrategies().apply(TraversalEngine.STANDARD);
        List<Step> steps = traversal.asAdmin().getSteps();
        Set<Class<? extends Step>> expSteps = Sets.newHashSet(expectedStepTypes);
        int numSteps = 0;
        for (Step s : steps) {
//            System.out.println(s.getClass());
            if (s.getClass().equals(StartStep.class)) continue;

            assertTrue(s.getClass().getName(),expSteps.contains(s.getClass()));
            numSteps++;
        }
        assertEquals(expectedSteps,numSteps);
    }




   /* ==================================================================================
                            LOGGING
     ==================================================================================*/


    @Test
    public void simpleLogTest() throws InterruptedException {
        simpleLogTest(false);
    }

    @Test
    public void simpleLogTestWithFailure() throws InterruptedException {
        simpleLogTest(true);
    }

    public void simpleLogTest(final boolean withLogFailure) throws InterruptedException {
        final String userlogName = "test";
        final Serializer serializer = graph.getDataSerializer();
        final EdgeSerializer edgeSerializer = graph.getEdgeSerializer();
        final TimestampProvider times = graph.getConfiguration().getTimestampProvider();
        final TimeUnit unit = times.getUnit();
        final long startTime = times.getTime().getTimestamp(TimeUnit.MILLISECONDS);
        clopen( option(SYSTEM_LOG_TRANSACTIONS), true,
                option(LOG_BACKEND, USER_LOG),(withLogFailure?TestMockLog.class.getName():LOG_BACKEND.getDefaultValue()),
                option(TestMockLog.LOG_MOCK_FAILADD, USER_LOG),withLogFailure,
                option(KCVSLog.LOG_READ_LAG_TIME, USER_LOG),new StandardDuration(50,TimeUnit.MILLISECONDS),
                option(LOG_READ_INTERVAL, USER_LOG),new StandardDuration(250,TimeUnit.MILLISECONDS),
                option(LOG_SEND_DELAY, USER_LOG),new StandardDuration(100,TimeUnit.MILLISECONDS),
                option(KCVSLog.LOG_READ_LAG_TIME,TRANSACTION_LOG),new StandardDuration(50,TimeUnit.MILLISECONDS),
                option(LOG_READ_INTERVAL,TRANSACTION_LOG),new StandardDuration(250,TimeUnit.MILLISECONDS),
                option(MAX_COMMIT_TIME),new StandardDuration(1,TimeUnit.SECONDS)
        );
        final String instanceid = graph.getConfiguration().getUniqueGraphId();

        PropertyKey weight = tx.makePropertyKey("weight").dataType(Decimal.class).cardinality(Cardinality.SINGLE).make();
        EdgeLabel knows = tx.makeEdgeLabel("knows").make();
        TitanVertex n1 = tx.addVertex("weight", 10.5);
        newTx();

        final Timepoint txTimes[] = new Timepoint[4];
        //Transaction with custom userlog name
        txTimes[0] = times.getTime();
        TitanTransaction tx2 = graph.buildTransaction().logIdentifier(userlogName).start();
        TitanVertex v1 = tx2.addVertex("weight", 111.1);
        v1.addEdge("knows", v1);
        tx2.commit();
        final long v1id = getId(v1);
        txTimes[1] = times.getTime();
        tx2 = graph.buildTransaction().logIdentifier(userlogName).start();
        TitanVertex v2 = tx2.addVertex("weight",222.2);
        v2.addEdge("knows",getV(tx2,v1id));
        tx2.commit();
        final long v2id = getId(v2);
        //Only read tx
        tx2 = graph.buildTransaction().logIdentifier(userlogName).start();
        v1 = getV(tx2,v1id);
        assertEquals(111.1,v1.<Decimal>value("weight").doubleValue(),0.0);
        assertEquals(222.2,getV(tx2,v2).<Decimal>value("weight").doubleValue(),0.0);
        tx2.commit();
        //Deleting transaction
        txTimes[2] = times.getTime();
        tx2 = graph.buildTransaction().logIdentifier(userlogName).start();
        v2 = getV(tx2,v2id);
        assertEquals(222.2,v2.<Decimal>value("weight").doubleValue(),0.0);
        v2.remove();
        tx2.commit();
        //Edge modifying transaction
        txTimes[3] = times.getTime();
        tx2 = graph.buildTransaction().logIdentifier(userlogName).start();
        v1 = getV(tx2,v1id);
        assertEquals(111.1,v1.<Decimal>value("weight").doubleValue(),0.0);
        Edge e = getOnlyElement(v1.query().direction(Direction.OUT).labels("knows").edges());
        assertFalse(e.property("weight").isPresent());
        e.property("weight", 44.4);
        tx2.commit();
        close();
        final long endTime = times.getTime().getTimestamp(TimeUnit.MILLISECONDS);

        final ReadMarker startMarker = ReadMarker.fromTime(startTime, TimeUnit.MILLISECONDS);

        Log txlog = openTxLog();
        Log userLog = openUserLog(userlogName);
        final EnumMap<LogTxStatus,AtomicInteger> txMsgCounter = new EnumMap<LogTxStatus,AtomicInteger>(LogTxStatus.class);
        for (LogTxStatus status : LogTxStatus.values()) txMsgCounter.put(status,new AtomicInteger(0));
        final AtomicInteger userlogMeta = new AtomicInteger(0);
        txlog.registerReader(startMarker,new MessageReader() {
            @Override
            public void read(Message message) {
                long msgTime = message.getTimestamp(TimeUnit.MILLISECONDS);
                assertTrue(msgTime>=startTime);
                assertNotNull(message.getSenderId());
                TransactionLogHeader.Entry txEntry = TransactionLogHeader.parse(message.getContent(),serializer, times);
                TransactionLogHeader header = txEntry.getHeader();
//                System.out.println(header.getTimestamp(TimeUnit.MILLISECONDS));
                assertTrue(header.getTimestamp(TimeUnit.MILLISECONDS) >= startTime);
                assertTrue(header.getTimestamp(TimeUnit.MILLISECONDS)<=msgTime);
                assertNotNull(txEntry.getMetadata());
                assertNull(txEntry.getMetadata().get(LogTxMeta.GROUPNAME));
                LogTxStatus status = txEntry.getStatus();
                if (status==LogTxStatus.PRECOMMIT) {
                    assertTrue(txEntry.hasContent());
                    Object logid = txEntry.getMetadata().get(LogTxMeta.LOG_ID);
                    if (logid!=null) {
                        assertTrue(logid instanceof String);
                        assertEquals(userlogName,logid);
                        userlogMeta.incrementAndGet();
                    }
                } else if (withLogFailure) {
                    assertTrue(status.isPrimarySuccess() || status==LogTxStatus.SECONDARY_FAILURE);
                    if (status==LogTxStatus.SECONDARY_FAILURE) {
                        TransactionLogHeader.SecondaryFailures secFail = txEntry.getContentAsSecondaryFailures(serializer);
                        assertTrue(secFail.failedIndexes.isEmpty());
                        assertTrue(secFail.userLogFailure);
                    }
                } else {
                    assertFalse(txEntry.hasContent());
                    assertTrue(status.isSuccess());
                }
                txMsgCounter.get(txEntry.getStatus()).incrementAndGet();
            }
        });
        final EnumMap<Change,AtomicInteger> userChangeCounter = new EnumMap<Change,AtomicInteger>(Change.class);
        for (Change change : Change.values()) userChangeCounter.put(change,new AtomicInteger(0));
        final AtomicInteger userLogMsgCounter = new AtomicInteger(0);
        userLog.registerReader(startMarker, new MessageReader() {
            @Override
            public void read(Message message) {
                long msgTime = message.getTimestamp(TimeUnit.MILLISECONDS);
                assertTrue(msgTime >= startTime);
                assertNotNull(message.getSenderId());
                StaticBuffer content = message.getContent();
                assertTrue(content != null && content.length() > 0);
                TransactionLogHeader.Entry txentry = TransactionLogHeader.parse(content, serializer, times);

                long txTime = txentry.getHeader().getTimestamp(TimeUnit.MILLISECONDS);
                assertTrue(txTime <= msgTime);
                assertTrue(txTime >= startTime);
                long txid = txentry.getHeader().getId();
                assertTrue(txid > 0);
                for (TransactionLogHeader.Modification modification : txentry.getContentAsModifications(serializer)) {
                    assertTrue(modification.state == Change.ADDED || modification.state == Change.REMOVED);
                    userChangeCounter.get(modification.state).incrementAndGet();
                }
                userLogMsgCounter.incrementAndGet();
            }
        });
        Thread.sleep(4000);
        assertEquals(5,txMsgCounter.get(LogTxStatus.PRECOMMIT).get());
        assertEquals(4,txMsgCounter.get(LogTxStatus.PRIMARY_SUCCESS).get());
        assertEquals(1,txMsgCounter.get(LogTxStatus.COMPLETE_SUCCESS).get());
        assertEquals(4, userlogMeta.get());
        if (withLogFailure) assertEquals(4,txMsgCounter.get(LogTxStatus.SECONDARY_FAILURE).get());
        else assertEquals(4,txMsgCounter.get(LogTxStatus.SECONDARY_SUCCESS).get());
        //User-Log
        if (withLogFailure) {
            assertEquals(0, userLogMsgCounter.get());
        } else {
            assertEquals(4, userLogMsgCounter.get());
            assertEquals(7, userChangeCounter.get(Change.ADDED).get());
            assertEquals(4,userChangeCounter.get(Change.REMOVED).get());
        }

        clopen( option(VERBOSE_TX_RECOVERY), true );
        /*
        Transaction Recovery
         */
        TransactionRecovery recovery = TitanFactory.startTransactionRecovery(graph,startTime,TimeUnit.MILLISECONDS);


        /*
        Use user log processing framework
         */
        final AtomicInteger userLogCount = new AtomicInteger(0);
        LogProcessorFramework userlogs = TitanFactory.openTransactionLog(graph);
        userlogs.addLogProcessor(userlogName).setStartTime(startTime, TimeUnit.MILLISECONDS).setRetryAttempts(1)
        .addProcessor(new ChangeProcessor() {
            @Override
            public void process(TitanTransaction tx, TransactionId txId, ChangeState changes) {
                assertEquals(instanceid, txId.getInstanceId());
                assertTrue(txId.getTransactionId() > 0 && txId.getTransactionId() < 100); //Just some reasonable upper bound
                final long txTime = txId.getTransactionTime().sinceEpoch(TimeUnit.MILLISECONDS);
                assertTrue(String.format("tx timestamp %s not between start %s and end time %s",
                                txTime, startTime, endTime),
                        txTime >= startTime && txTime <= endTime); //Times should be within a second

                assertTrue(tx.containsRelationType("knows"));
                assertTrue(tx.containsRelationType("weight"));
                EdgeLabel knows = tx.getEdgeLabel("knows");
                PropertyKey weight = tx.getPropertyKey("weight");

                long txTimeMicro = txId.getTransactionTime().sinceEpoch(TimeUnit.MICROSECONDS);

                int txNo;
                if (txTimeMicro < txTimes[1].getTimestamp(TimeUnit.MICROSECONDS)) {
                    txNo = 1;
                    //v1 addition transaction
                    assertEquals(1, Iterables.size(changes.getVertices(Change.ADDED)));
                    assertEquals(0, Iterables.size(changes.getVertices(Change.REMOVED)));
                    assertEquals(1, Iterables.size(changes.getVertices(Change.ANY)));
                    assertEquals(2, Iterables.size(changes.getRelations(Change.ADDED)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.ADDED, knows)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.ADDED, weight)));
                    assertEquals(2, Iterables.size(changes.getRelations(Change.ANY)));
                    assertEquals(0, Iterables.size(changes.getRelations(Change.REMOVED)));

                    TitanVertex v = Iterables.getOnlyElement(changes.getVertices(Change.ADDED));
                    assertEquals(v1id, getId(v));
                    VertexProperty<Decimal> p = Iterables.getOnlyElement(changes.getProperties(v, Change.ADDED, "weight"));
                    assertEquals(111.1, p.value().doubleValue(), 0.0001);
                    assertEquals(1, Iterables.size(changes.getEdges(v, Change.ADDED, OUT)));
                    assertEquals(1, Iterables.size(changes.getEdges(v, Change.ADDED, BOTH)));
                } else if (txTimeMicro < txTimes[2].getTimestamp(TimeUnit.MICROSECONDS)) {
                    txNo = 2;
                    //v2 addition transaction
                    assertEquals(1, Iterables.size(changes.getVertices(Change.ADDED)));
                    assertEquals(0, Iterables.size(changes.getVertices(Change.REMOVED)));
                    assertEquals(2, Iterables.size(changes.getVertices(Change.ANY)));
                    assertEquals(2, Iterables.size(changes.getRelations(Change.ADDED)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.ADDED, knows)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.ADDED, weight)));
                    assertEquals(2, Iterables.size(changes.getRelations(Change.ANY)));
                    assertEquals(0, Iterables.size(changes.getRelations(Change.REMOVED)));

                    TitanVertex v = Iterables.getOnlyElement(changes.getVertices(Change.ADDED));
                    assertEquals(v2id, getId(v));
                    VertexProperty<Decimal> p = Iterables.getOnlyElement(changes.getProperties(v, Change.ADDED, "weight"));
                    assertEquals(222.2, p.value().doubleValue(), 0.0001);
                    assertEquals(1, Iterables.size(changes.getEdges(v, Change.ADDED, OUT)));
                    assertEquals(1, Iterables.size(changes.getEdges(v, Change.ADDED, BOTH)));
                } else if (txTimeMicro < txTimes[3].getTimestamp(TimeUnit.MICROSECONDS)) {
                    txNo = 3;
                    //v2 deletion transaction
                    assertEquals(0, Iterables.size(changes.getVertices(Change.ADDED)));
                    assertEquals(1, Iterables.size(changes.getVertices(Change.REMOVED)));
                    assertEquals(2, Iterables.size(changes.getVertices(Change.ANY)));
                    assertEquals(0, Iterables.size(changes.getRelations(Change.ADDED)));
                    assertEquals(2, Iterables.size(changes.getRelations(Change.REMOVED)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.REMOVED, knows)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.REMOVED, weight)));
                    assertEquals(2, Iterables.size(changes.getRelations(Change.ANY)));

                    TitanVertex v = Iterables.getOnlyElement(changes.getVertices(Change.REMOVED));
                    assertEquals(v2id, getId(v));
                    VertexProperty<Decimal> p = Iterables.getOnlyElement(changes.getProperties(v, Change.REMOVED, "weight"));
                    assertEquals(222.2, p.value().doubleValue(), 0.0001);
                    assertEquals(1, Iterables.size(changes.getEdges(v, Change.REMOVED, OUT)));
                    assertEquals(0, Iterables.size(changes.getEdges(v, Change.ADDED, BOTH)));
                } else {
                    txNo = 4;
                    //v1 edge modification
                    assertEquals(0, Iterables.size(changes.getVertices(Change.ADDED)));
                    assertEquals(0, Iterables.size(changes.getVertices(Change.REMOVED)));
                    assertEquals(1, Iterables.size(changes.getVertices(Change.ANY)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.ADDED)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.REMOVED)));
                    assertEquals(1, Iterables.size(changes.getRelations(Change.REMOVED, knows)));
                    assertEquals(2, Iterables.size(changes.getRelations(Change.ANY)));

                    TitanVertex v = Iterables.getOnlyElement(changes.getVertices(Change.ANY));
                    assertEquals(v1id, getId(v));
                    TitanEdge e = Iterables.getOnlyElement(changes.getEdges(v, Change.REMOVED, Direction.OUT, "knows"));
                    assertFalse(e.property("weight").isPresent());
                    assertEquals(v, e.vertex(Direction.IN));
                    e = Iterables.getOnlyElement(changes.getEdges(v, Change.ADDED, Direction.OUT, "knows"));
                    assertEquals(44.4, e.<Decimal>value("weight").doubleValue(), 0.0);
                    assertEquals(v, e.vertex(Direction.IN));
                }

                //See only current state of graph in transaction
                TitanVertex v1 = getV(tx,v1id);
                assertNotNull(v1);
                assertTrue(v1.isLoaded());
                if (txNo != 2) {
                    //In the transaction that adds v2, v2 will be considered "loaded"
                    assertMissing(tx,v2id);
//                    assertTrue(txNo + " - " + v2, v2 == null || v2.isRemoved());
                }
                assertEquals(111.1, v1.<Decimal>value("weight").doubleValue(), 0.0);
                assertCount(1, v1.query().direction(Direction.OUT).edges());

                userLogCount.incrementAndGet();
            }
        }).build();

        //wait
        Thread.sleep(22000L);

        recovery.shutdown();
        long[] recoveryStats = ((StandardTransactionLogProcessor)recovery).getStatistics();
        if (withLogFailure) {
            assertEquals(1,recoveryStats[0]);
            assertEquals(4,recoveryStats[1]);
        } else {
            assertEquals(5,recoveryStats[0]);
            assertEquals(0,recoveryStats[1]);

        }

        userlogs.removeLogProcessor(userlogName);
        userlogs.shutdown();
        assertEquals(4, userLogCount.get());
    }


   /* ==================================================================================
                            GLOBAL GRAPH QUERIES
     ==================================================================================*/


    /**
     * Tests index defintions and their correct application for internal indexes only
     */
    @Test
    public void testGlobalGraphIndexingAndQueriesForInternalIndexes() {
        PropertyKey weight = makeKey("weight",Decimal.class);
        PropertyKey time = makeKey("time",Long.class);
        PropertyKey text = makeKey("text",String.class);

        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.LIST).make();
        EdgeLabel connect = mgmt.makeEdgeLabel("connect").signature(weight).make();
        EdgeLabel related = mgmt.makeEdgeLabel("related").signature(time).make();

        VertexLabel person = mgmt.makeVertexLabel("person").make();
        VertexLabel organization = mgmt.makeVertexLabel("organization").make();

        TitanGraphIndex edge1 = mgmt.buildIndex("edge1",Edge.class).addKey(time).addKey(weight).buildCompositeIndex();
        TitanGraphIndex edge2 = mgmt.buildIndex("edge2",Edge.class).indexOnly(connect).addKey(text).buildCompositeIndex();

        TitanGraphIndex prop1 = mgmt.buildIndex("prop1",TitanVertexProperty.class).addKey(time).buildCompositeIndex();
        TitanGraphIndex prop2 = mgmt.buildIndex("prop2",TitanVertexProperty.class).addKey(weight).addKey(text).buildCompositeIndex();

        TitanGraphIndex vertex1 = mgmt.buildIndex("vertex1",Vertex.class).addKey(time).indexOnly(person).unique().buildCompositeIndex();
        TitanGraphIndex vertex12 = mgmt.buildIndex("vertex12", Vertex.class).addKey(text).indexOnly(person).buildCompositeIndex();
        TitanGraphIndex vertex2 = mgmt.buildIndex("vertex2",Vertex.class).addKey(time).addKey(name).indexOnly(organization).buildCompositeIndex();
        TitanGraphIndex vertex3 = mgmt.buildIndex("vertex3",Vertex.class).addKey(name).buildCompositeIndex();


        // ########### INSPECTION & FAILURE ##############
        assertTrue(mgmt.containsRelationType("name"));
        assertTrue(mgmt.containsGraphIndex("prop1"));
        assertFalse(mgmt.containsGraphIndex("prop3"));
        assertEquals(2,Iterables.size(mgmt.getGraphIndexes(Edge.class)));
        assertEquals(2,Iterables.size(mgmt.getGraphIndexes(TitanVertexProperty.class)));
        assertEquals(4,Iterables.size(mgmt.getGraphIndexes(Vertex.class)));
        assertNull(mgmt.getGraphIndex("balblub"));

        edge1 = mgmt.getGraphIndex("edge1");
        edge2 = mgmt.getGraphIndex("edge2");
        prop1 = mgmt.getGraphIndex("prop1");
        prop2 = mgmt.getGraphIndex("prop2");
        vertex1 = mgmt.getGraphIndex("vertex1");
        vertex12 = mgmt.getGraphIndex("vertex12");
        vertex2 = mgmt.getGraphIndex("vertex2");
        vertex3 = mgmt.getGraphIndex("vertex3");

        assertTrue(vertex1.isUnique());
        assertFalse(edge2.isUnique());
        assertEquals("prop1",prop1.name());
        assertTrue(Vertex.class.isAssignableFrom(vertex3.getIndexedElement()));
        assertTrue(TitanVertexProperty.class.isAssignableFrom(prop1.getIndexedElement()));
        assertTrue(Edge.class.isAssignableFrom(edge2.getIndexedElement()));
        assertEquals(2,vertex2.getFieldKeys().length);
        assertEquals(1,vertex1.getFieldKeys().length);

        try {
            //Parameters not supported
            mgmt.buildIndex("blablub",Vertex.class).addKey(text, Mapping.TEXT.asParameter()).buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Name already in use
            mgmt.buildIndex("edge1",Vertex.class).addKey(weight).buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //ImplicitKeys not allowed
            mgmt.buildIndex("jupdup",Vertex.class).addKey(ImplicitKey.ID).buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Unique is only allowed for vertex
            mgmt.buildIndex("edgexyz",Edge.class).addKey(time).unique().buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}

        // ########### END INSPECTION & FAILURE ##############
        finishSchema();
        clopen();

        text = mgmt.getPropertyKey("text");
        time = mgmt.getPropertyKey("time");
        weight = mgmt.getPropertyKey("weight");

        // ########### INSPECTION & FAILURE (copied from above) ##############
        assertTrue(mgmt.containsRelationType("name"));
        assertTrue(mgmt.containsGraphIndex("prop1"));
        assertFalse(mgmt.containsGraphIndex("prop3"));
        assertEquals(2,Iterables.size(mgmt.getGraphIndexes(Edge.class)));
        assertEquals(2,Iterables.size(mgmt.getGraphIndexes(TitanVertexProperty.class)));
        assertEquals(4,Iterables.size(mgmt.getGraphIndexes(Vertex.class)));
        assertNull(mgmt.getGraphIndex("balblub"));

        edge1 = mgmt.getGraphIndex("edge1");
        edge2 = mgmt.getGraphIndex("edge2");
        prop1 = mgmt.getGraphIndex("prop1");
        prop2 = mgmt.getGraphIndex("prop2");
        vertex1 = mgmt.getGraphIndex("vertex1");
        vertex12 = mgmt.getGraphIndex("vertex12");
        vertex2 = mgmt.getGraphIndex("vertex2");
        vertex3 = mgmt.getGraphIndex("vertex3");

        assertTrue(vertex1.isUnique());
        assertFalse(edge2.isUnique());
        assertEquals("prop1",prop1.name());
        assertTrue(Vertex.class.isAssignableFrom(vertex3.getIndexedElement()));
        assertTrue(TitanVertexProperty.class.isAssignableFrom(prop1.getIndexedElement()));
        assertTrue(Edge.class.isAssignableFrom(edge2.getIndexedElement()));
        assertEquals(2,vertex2.getFieldKeys().length);
        assertEquals(1,vertex1.getFieldKeys().length);

        try {
            //Parameters not supported
            mgmt.buildIndex("blablub",Vertex.class).addKey(text, Mapping.TEXT.asParameter()).buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Name already in use
            mgmt.buildIndex("edge1",Vertex.class).addKey(weight).buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //ImplicitKeys not allowed
            mgmt.buildIndex("jupdup",Vertex.class).addKey(ImplicitKey.ID).buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Unique is only allowed for vertex
            mgmt.buildIndex("edgexyz",Edge.class).addKey(time).unique().buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}

        // ########### END INSPECTION & FAILURE ##############

        final int numV = 100;
        final boolean sorted = true;
        TitanVertex ns[] = new TitanVertex[numV];
        String[] strs = {"aaa","bbb","ccc","ddd"};

        for (int i=0;i<numV;i++) {
            ns[i]=tx.addVertex(i % 2 == 0 ? "person" : "organization");
            VertexProperty p1 = ns[i].property("name","v"+i);
            VertexProperty p2 = ns[i].property("name","u"+(i%5));

            double w = (i*0.5)%5;
            long t = i;
            String txt = strs[i%(strs.length)];

            ns[i].property(VertexProperty.Cardinality.single, "weight", w);
            ns[i].property(VertexProperty.Cardinality.single, "time", t);
            ns[i].property(VertexProperty.Cardinality.single, "text", txt);

            for (VertexProperty p : new VertexProperty[]{p1,p2}) {
                p.property("weight",w);
                p.property("time",t);
                p.property("text",txt);
            }

            TitanVertex u = ns[(i>0?i-1:i)]; //previous or self-loop
            for (String label : new String[]{"connect","related"}) {
                Edge e = ns[i].addEdge(label,u, "weight",(w++)%5, "time",t, "text",txt);
            }
        }

        //########## QUERIES ################
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,10).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,1,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Contain.IN,ImmutableList.of(10,20,30)).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,3,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,10).has("weight",Cmp.EQUAL,0).has("text",Cmp.EQUAL,strs[10%strs.length]),
                ElementCategory.EDGE,1,new boolean[]{false,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,10).has("weight",Cmp.EQUAL,1),
                ElementCategory.EDGE,1,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,20).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,1,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,20).has("weight",Cmp.EQUAL,3),
                ElementCategory.EDGE,0,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[0]).has(LABEL_NAME,"connect"),
                ElementCategory.EDGE,numV/strs.length,new boolean[]{true,sorted},edge2.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[0]).has(LABEL_NAME,"connect").limit(10),
                ElementCategory.EDGE,10,new boolean[]{true,sorted},edge2.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[0]),
                ElementCategory.EDGE,numV/strs.length*2,new boolean[]{false,sorted});
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,1.5),
                ElementCategory.EDGE,numV/10*2,new boolean[]{false,sorted});

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,50),
                ElementCategory.PROPERTY,2,new boolean[]{true,sorted},prop1.name());
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,0.0).has("text",Cmp.EQUAL,strs[0]),
                ElementCategory.PROPERTY,2*numV/(4*5),new boolean[]{true,sorted},prop2.name());
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,0.0).has("text",Cmp.EQUAL,strs[0]).has("time",Cmp.EQUAL,0),
                ElementCategory.PROPERTY,2,new boolean[]{true,sorted},prop2.name(),prop1.name());
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,1.5),
                ElementCategory.PROPERTY,2*numV/10,new boolean[]{false,sorted});

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,50).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex1.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[2]).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,numV/strs.length,new boolean[]{true,sorted},vertex12.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[3]).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,0,new boolean[]{true,sorted},vertex12.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[2]).has(LABEL_NAME,"person").has("time",Cmp.EQUAL,2),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex12.name(),vertex1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,51).has("name",Cmp.EQUAL,"v51").has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex2.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,51).has("name",Cmp.EQUAL,"u1").has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex2.name());
        evaluateQuery(tx.query().has("time",Contain.IN,ImmutableList.of(51,61,71,31,41)).has("name",Cmp.EQUAL,"u1").has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,5,new boolean[]{true,sorted},vertex2.name());
        evaluateQuery(tx.query().has("time",Contain.IN,ImmutableList.of()),
                ElementCategory.VERTEX,0,new boolean[]{true,false});
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[2]).has(LABEL_NAME,"person").has("time",Contain.NOT_IN,ImmutableList.of()),
                ElementCategory.VERTEX,numV/strs.length,new boolean[]{true,sorted},vertex12.name());


        evaluateQuery(tx.query().has("time",Cmp.EQUAL,51).has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,1,new boolean[]{false,sorted});
        evaluateQuery(tx.query().has("name",Cmp.EQUAL,"u1"),
                ElementCategory.VERTEX,numV/5,new boolean[]{true,sorted},vertex3.name());
        evaluateQuery(tx.query().has("name",Cmp.EQUAL,"v1"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex3.name());
        evaluateQuery(tx.query().has("name",Cmp.EQUAL,"v1").has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,1,new boolean[]{false,sorted},vertex3.name());

        clopen();

        //########## QUERIES (copied from above) ################
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,10).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,1,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Contain.IN,ImmutableList.of(10,20,30)).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,3,new boolean[]{true,sorted},edge1.name());

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,10).has("weight",Cmp.EQUAL,0).has("text",Cmp.EQUAL,strs[10%strs.length]),
                ElementCategory.EDGE,1,new boolean[]{false,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,10).has("weight",Cmp.EQUAL,1),
                ElementCategory.EDGE,1,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,20).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,1,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,20).has("weight",Cmp.EQUAL,3),
                ElementCategory.EDGE,0,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[0]).has(LABEL_NAME,"connect"),
                ElementCategory.EDGE,numV/strs.length,new boolean[]{true,sorted},edge2.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[0]).has(LABEL_NAME,"connect").limit(10),
                ElementCategory.EDGE,10,new boolean[]{true,sorted},edge2.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[0]),
                ElementCategory.EDGE,numV/strs.length*2,new boolean[]{false,sorted});
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,1.5),
                ElementCategory.EDGE,numV/10*2,new boolean[]{false,sorted});

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,50),
                ElementCategory.PROPERTY,2,new boolean[]{true,sorted},prop1.name());
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,0.0).has("text",Cmp.EQUAL,strs[0]),
                ElementCategory.PROPERTY,2*numV/(4*5),new boolean[]{true,sorted},prop2.name());
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,0.0).has("text",Cmp.EQUAL,strs[0]).has("time",Cmp.EQUAL,0),
                ElementCategory.PROPERTY,2,new boolean[]{true,sorted},prop2.name(),prop1.name());
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,1.5),
                ElementCategory.PROPERTY,2*numV/10,new boolean[]{false,sorted});

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,50).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex1.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[2]).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,numV/strs.length,new boolean[]{true,sorted},vertex12.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[3]).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,0,new boolean[]{true,sorted},vertex12.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[2]).has(LABEL_NAME,"person").has("time",Cmp.EQUAL,2),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex12.name(),vertex1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,51).has("name",Cmp.EQUAL,"v51").has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex2.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,51).has("name",Cmp.EQUAL,"u1").has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex2.name());
        evaluateQuery(tx.query().has("time",Contain.IN,ImmutableList.of(51,61,71,31,41)).has("name",Cmp.EQUAL,"u1").has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,5,new boolean[]{true,sorted},vertex2.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,51).has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,1,new boolean[]{false,sorted});
        evaluateQuery(tx.query().has("name",Cmp.EQUAL,"u1"),
                ElementCategory.VERTEX,numV/5,new boolean[]{true,sorted},vertex3.name());
        evaluateQuery(tx.query().has("name",Cmp.EQUAL,"v1"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex3.name());
        evaluateQuery(tx.query().has("name",Cmp.EQUAL,"v1").has(LABEL_NAME,"organization"),
                ElementCategory.VERTEX,1,new boolean[]{false,sorted},vertex3.name());
        evaluateQuery(tx.query().has("time",Contain.IN,ImmutableList.of()),
                ElementCategory.VERTEX,0,new boolean[]{true,false});
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[2]).has(LABEL_NAME,"person").has("time",Contain.NOT_IN,ImmutableList.of()),
                ElementCategory.VERTEX,numV/strs.length,new boolean[]{true,sorted},vertex12.name());

        //Update in transaction
        for (int i=0;i<numV/2;i++) {
            TitanVertex v = getV(tx,ns[i]);
            v.remove();
        }
        ns = new TitanVertex[numV*3/2];
        for (int i=numV;i<numV*3/2;i++) {
            ns[i]=tx.addVertex(i % 2 == 0 ? "person" : "organization");
            VertexProperty p1 = ns[i].property("name","v"+i);
            VertexProperty p2 = ns[i].property("name","u"+(i%5));

            double w = (i*0.5)%5;
            long t = i;
            String txt = strs[i%(strs.length)];

            ns[i].property(VertexProperty.Cardinality.single, "weight", w);
            ns[i].property(VertexProperty.Cardinality.single, "time", t);
            ns[i].property(VertexProperty.Cardinality.single, "text", txt);

            for (VertexProperty p : new VertexProperty[]{p1,p2}) {
                p.property("weight",w);
                p.property("time",t);
                p.property("text",txt);
            }

            TitanVertex u = ns[(i>numV?i-1:i)]; //previous or self-loop
            for (String label : new String[]{"connect","related"}) {
                Edge e = ns[i].addEdge(label,u,"weight",(w++)%5,"time",t,"text",txt);
            }
        }


        //######### UPDATED QUERIES ##########

        evaluateQuery(tx.query().has("time", Cmp.EQUAL, 10).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,0,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time", Cmp.EQUAL, numV + 10).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,1,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[0]).has(LABEL_NAME,"connect").limit(10),
                ElementCategory.EDGE,10,new boolean[]{true,sorted},edge2.name());
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,1.5),
                ElementCategory.EDGE,numV/10*2,new boolean[]{false,sorted});

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,20),
                ElementCategory.PROPERTY,0,new boolean[]{true,sorted},prop1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,numV+20),
                ElementCategory.PROPERTY,2,new boolean[]{true,sorted},prop1.name());

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,30).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,0,new boolean[]{true,sorted},vertex1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,numV+30).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex1.name());
        evaluateQuery(tx.query().has("name",Cmp.EQUAL,"u1"),
                ElementCategory.VERTEX,numV/5,new boolean[]{true,sorted},vertex3.name());


        //######### END UPDATED QUERIES ##########

        newTx();

        //######### UPDATED QUERIES (copied from above) ##########
        evaluateQuery(tx.query().has("time", Cmp.EQUAL, 10).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,0,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("time", Cmp.EQUAL, numV + 10).has("weight",Cmp.EQUAL,0),
                ElementCategory.EDGE,1,new boolean[]{true,sorted},edge1.name());
        evaluateQuery(tx.query().has("text",Cmp.EQUAL,strs[0]).has(LABEL_NAME,"connect").limit(10),
                ElementCategory.EDGE,10,new boolean[]{true,sorted},edge2.name());
        evaluateQuery(tx.query().has("weight",Cmp.EQUAL,1.5),
                ElementCategory.EDGE,numV/10*2,new boolean[]{false,sorted});

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,20),
                ElementCategory.PROPERTY,0,new boolean[]{true,sorted},prop1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,numV+20),
                ElementCategory.PROPERTY,2,new boolean[]{true,sorted},prop1.name());

        evaluateQuery(tx.query().has("time",Cmp.EQUAL,30).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,0,new boolean[]{true,sorted},vertex1.name());
        evaluateQuery(tx.query().has("time",Cmp.EQUAL,numV+30).has(LABEL_NAME,"person"),
                ElementCategory.VERTEX,1,new boolean[]{true,sorted},vertex1.name());
        evaluateQuery(tx.query().has("name",Cmp.EQUAL,"u1"),
                ElementCategory.VERTEX,numV/5,new boolean[]{true,sorted},vertex3.name());

        //*** INIVIDUAL USE CASE TESTS ******


    }

    @Test
    public void testIndexUniqueness() {
        PropertyKey time = makeKey("time", Long.class);
        PropertyKey text = makeKey("text", String.class);

        VertexLabel person = mgmt.makeVertexLabel("person").make();
        VertexLabel org = mgmt.makeVertexLabel("organization").make();

        TitanGraphIndex vindex1 = mgmt.buildIndex("vindex1",Vertex.class).addKey(time).indexOnly(person).unique().buildCompositeIndex();
        TitanGraphIndex vindex2 = mgmt.buildIndex("vindex2",Vertex.class).addKey(time).addKey(text).unique().buildCompositeIndex();
        finishSchema();

        //================== VERTEX UNIQUENESS ====================

        //I) Label uniqueness
        //Ia) Uniqueness violation in same transaction
        failTransactionOnCommit(new TransactionJob() {
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex v0 = tx.addVertex("person");
                v0.property(VertexProperty.Cardinality.single, "time", 1);
                TitanVertex v1 = tx.addVertex("person");
                v1.property(VertexProperty.Cardinality.single, "time", 1);
            }
        });

        //Ib) Uniqueness violation across transactions
        TitanVertex v0 = tx.addVertex("person");
        v0.property(VertexProperty.Cardinality.single, "time", 1);
        newTx();
        failTransactionOnCommit(new TransactionJob() {
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex v1 = tx.addVertex("person");
                v1.property(VertexProperty.Cardinality.single, "time", 1);
            }
        });
        //Ic) However, this should work since the label is different
        TitanVertex v1 = tx.addVertex("organization");
        v1.property(VertexProperty.Cardinality.single, "time", 1);
        newTx();

        //II) Composite uniqueness
        //IIa) Uniqueness violation in same transaction
        failTransactionOnCommit(new TransactionJob() {
            @Override
            public void run(TitanTransaction tx) {
                TitanVertex v0 = tx.addVertex("time",2,"text","hello");
                TitanVertex v1 = tx.addVertex("time",2,"text","hello");
            }
        });

        //IIb) Uniqueness violation across transactions
        v0 = tx.addVertex("time",2,"text","hello");
        newTx();
        failTransactionOnCommit(new TransactionJob() {
            @Override
            public void run(TitanTransaction tx) {

                TitanVertex v1 = tx.addVertex("time",2,"text","hello");
            }
        });


    }

    public static void evaluateQuery(TitanGraphQuery query, ElementCategory resultType,
                               int expectedResults, boolean[] subQuerySpecs,
                               PropertyKey orderKey1, Order order1,
                               String... intersectingIndexes) {
        evaluateQuery(query,resultType,expectedResults,subQuerySpecs,ImmutableMap.of(orderKey1,order1),intersectingIndexes);
    }

    public static void evaluateQuery(TitanGraphQuery query, ElementCategory resultType,
                               int expectedResults, boolean[] subQuerySpecs,
                               PropertyKey orderKey1, Order order1, PropertyKey orderKey2, Order order2,
                               String... intersectingIndexes) {
        evaluateQuery(query,resultType,expectedResults,subQuerySpecs,ImmutableMap.of(orderKey1,order1,orderKey2,order2),intersectingIndexes);
    }

    public static void evaluateQuery(TitanGraphQuery query, ElementCategory resultType,
                               int expectedResults, boolean[] subQuerySpecs,
                               String... intersectingIndexes) {
        evaluateQuery(query,resultType,expectedResults,subQuerySpecs,ImmutableMap.<PropertyKey,Order>of(),intersectingIndexes);
    }

    public static void evaluateQuery(TitanGraphQuery query, ElementCategory resultType,
                               int expectedResults, boolean[] subQuerySpecs,
                               Map<PropertyKey,Order> orderMap, String... intersectingIndexes) {
        if (intersectingIndexes==null) intersectingIndexes=new String[0];
        QueryDescription qd;
        switch(resultType) {
            case PROPERTY: qd = query.describeForProperties(); break;
            case EDGE: qd = query.describeForEdges(); break;
            case VERTEX: qd = query.describeForVertices(); break;
            default: throw new AssertionError();
        }
        assertEquals(1,qd.getNoCombinedQueries());
        assertEquals(1,qd.getNoSubQueries());
        QueryDescription.SubQuery sq = qd.getSubQueries().get(0);
        assertNotNull(sq);
        if (subQuerySpecs.length==2) { //0=>fitted, 1=>ordered
            assertEquals(subQuerySpecs[0],sq.isFitted());
            assertEquals(subQuerySpecs[1],sq.isSorted());
        }
        StandardQueryDescription.StandardSubQuery ssq = (StandardQueryDescription.StandardSubQuery)sq;
        assertEquals(intersectingIndexes.length,ssq.numIntersectingQueries());
        assertEquals(Sets.newHashSet(intersectingIndexes),Sets.newHashSet(ssq.getIntersectingQueries()));
        //Check order
        OrderList orders = ((StandardQueryDescription)qd).getQueryOrder();
        assertNotNull(orders);
        assertEquals(orderMap.size(),orders.size());
        for (int i=0;i<orders.size();i++) {
            assertEquals(orderMap.get(orders.getKey(i)),orders.getOrder(i));
        }
        for (PropertyKey key : orderMap.keySet()) assertTrue(orders.containsKey(key));

        Iterable<? extends TitanElement> result;
        switch(resultType) {
            case PROPERTY: result = query.properties(); break;
            case EDGE: result = query.edges(); break;
            case VERTEX: result = query.vertices(); break;
            default: throw new AssertionError();
        }
        int no = 0;
        TitanElement previous = null;
        for (TitanElement e : result) {
            assertNotNull(e);
            no++;
            if (previous!=null && !orders.isEmpty()) {
                assertTrue(orders.compare(previous,e)<=0);
            }
            previous = e;
        }
        assertEquals(expectedResults,no);
    }

    @Test
    public void testForceIndexUsage() {
        PropertyKey age = makeKey("age",Integer.class);
        PropertyKey time = makeKey("time",Long.class);
        mgmt.buildIndex("time",Vertex.class).addKey(time).buildCompositeIndex();
        finishSchema();

        for (int i=1;i<=10;i++) {
            TitanVertex v = tx.addVertex("time",i,"age",i);
        }

        //Graph query with and with-out index support
        assertCount(1, tx.query().has("time", 5).vertices());
        assertCount(1, tx.query().has("age", 6).vertices());

        clopen(option(FORCE_INDEX_USAGE), true);
        //Query with-out index support should now throw exception
        assertCount(1,tx.query().has("time",5).vertices());
        try {
            assertCount(1,tx.query().has("age",6).vertices());
            fail();
        } catch (Exception e) {}
    }

    @Test
    public void testLargeJointIndexRetrieval() {
        makeVertexIndexedKey("sid",Integer.class);
        makeVertexIndexedKey("color",String.class);
        finishSchema();

        int sids = 17;
        String[] colors = {"blue", "red", "yellow", "brown", "green", "orange", "purple"};
        int multiplier = 200;
        int numV = sids * colors.length * multiplier;
        for (int i = 0; i < numV; i++) {
            TitanVertex v = graph.addVertex(
                    "color", colors[i % colors.length],
                    "sid", i % sids);
        }
        clopen();

        assertCount(numV / sids, graph.query().has("sid", 8).vertices());
        assertCount(numV / colors.length, graph.query().has("color", colors[2]).vertices());

        assertCount(multiplier, graph.query().has("sid", 11).has("color", colors[3]).vertices());
    }


    @Test
    public void testIndexQueryWithLabelsAndContainsIN() {
        // This test is based on the steps to reproduce #882

        String labelName = "labelName";

        VertexLabel label = mgmt.makeVertexLabel(labelName).make();
        PropertyKey uid = mgmt.makePropertyKey("uid").dataType(String.class).make();
        TitanGraphIndex uidCompositeIndex = mgmt.buildIndex("uidIndex", Vertex.class).indexOnly(label).addKey(uid).unique().buildCompositeIndex();
        mgmt.setConsistency(uidCompositeIndex, ConsistencyModifier.LOCK);
        finishSchema();

        TitanVertex foo = graph.addVertex(labelName);
        TitanVertex bar = graph.addVertex(labelName);
        foo.property("uid", "foo");
        bar.property("uid", "bar");
        graph.tx().commit();

        Iterable<TitanVertex> vertexes = graph.query()
                .has("uid", Contain.IN, ImmutableList.of("foo", "bar"))
                .has(LABEL_NAME, labelName)
                .vertices();
        assertEquals(2, Iterables.size(vertexes));
        for (TitanVertex v : vertexes) {
            assertEquals(labelName, v.vertexLabel().name());
        }
    }

    @Test
    public void testLimitWithMixedIndexCoverage() {
        final String vt = "vt";
        final String fn = "firstname";
        final String user = "user";
        final String alice = "alice";
        final String bob = "bob";

        PropertyKey vtk = makeVertexIndexedKey(vt,String.class);
        PropertyKey fnk = makeKey(fn,String.class);

        finishSchema();

        TitanVertex a = tx.addVertex(vt, user, fn, "alice");
        TitanVertex b = tx.addVertex(vt, user, fn, "bob");
        TitanVertex v;

        v = getOnlyElement(tx.query().has(vt, user).has(fn, bob).limit(1).vertices());
        assertEquals(bob, v.value(fn));
        assertEquals(user, v.value(vt));

        v = getOnlyElement(tx.query().has(vt, user).has(fn, alice).limit(1).vertices());
        assertEquals(alice, v.value(fn));
        assertEquals(user, v.value(vt));

        tx.commit();
        tx = graph.newTransaction();

        v = getOnlyElement(tx.query().has(vt, user).has(fn, bob).limit(1).vertices());
        assertEquals(bob, v.value(fn));
        assertEquals(user, v.value(vt));

        v = getOnlyElement(tx.query().has(vt, user).has(fn, alice).limit(1).vertices());
        assertEquals(alice, v.value(fn));
        assertEquals(user, v.value(vt));
    }

    @Test
    public void testWithoutIndex() {
        PropertyKey kid = mgmt.makePropertyKey("kid").dataType(Long.class).make();
        mgmt.makePropertyKey("name").dataType(String.class).make();
        mgmt.makeEdgeLabel("knows").signature(kid).make();
        finishSchema();

        Random random = new Random();
        int numV = 1000;
        TitanVertex previous = null;
        for (int i=0;i<numV;i++) {
            TitanVertex v = graph.addVertex(
                    "kid",random.nextInt(numV),"name","v"+i);
            if (previous!=null) {
                Edge e = v.addEdge("knows",previous,"kid",random.nextInt(numV/2));
            }
            previous=v;
        }

        verifyElementOrder(graph.query().orderBy("kid", incr).limit(500).vertices(), "kid", Order.ASC, 500);
        verifyElementOrder(graph.query().orderBy("kid", incr).limit(300).edges(), "kid", Order.ASC, 300);
        verifyElementOrder(graph.query().orderBy("kid", decr).limit(400).vertices(), "kid", Order.DESC, 400);
        verifyElementOrder(graph.query().orderBy("kid", decr).limit(200).edges(), "kid", Order.DESC, 200);

        clopen();

        //Copied from above
        verifyElementOrder(graph.query().orderBy("kid", incr).limit(500).vertices(), "kid", Order.ASC, 500);
        verifyElementOrder(graph.query().orderBy("kid", incr).limit(300).edges(), "kid", Order.ASC, 300);
        verifyElementOrder(graph.query().orderBy("kid", decr).limit(400).vertices(), "kid", Order.DESC, 400);
        verifyElementOrder(graph.query().orderBy("kid", decr).limit(200).edges(), "kid", Order.DESC, 200);
    }


    //................................................


    @Test
    public void testHasNot() {
        TitanVertex v1,v2;
        v1 = graph.addVertex();

        v2 = (TitanVertex)graph.query().hasNot("abcd").vertices().iterator().next();
        assertEquals(v1,v2);
        v2 = (TitanVertex)graph.query().hasNot("abcd",true).vertices().iterator().next();
        assertEquals(v1,v2);
    }

    @Test
    public void testVertexCentricIndexWithNull() {
        EdgeLabel bought = makeLabel("bought");
        PropertyKey time = makeKey("time",Long.class);
        mgmt.buildEdgeIndex(bought,"byTimeDesc",BOTH,decr,time);
        mgmt.buildEdgeIndex(bought,"byTimeIncr",BOTH,incr,time);
        finishSchema();

        TitanVertex v1 = tx.addVertex(), v2 = tx.addVertex();
        v1.addEdge("bought",v2).property("time",1);
        v1.addEdge("bought",v2).property("time",2);
        v1.addEdge("bought",v2).property("time",3);
        v1.addEdge("bought",v2);
        v1.addEdge("bought",v2);

        assertEquals(5,v1.query().direction(OUT).labels("bought").count());
        assertEquals(1,v1.query().direction(OUT).labels("bought").has("time",1).count());
        assertEquals(1,v1.query().direction(OUT).labels("bought").has("time",Cmp.LESS_THAN,3).has("time",Cmp.GREATER_THAN,1).count());
        assertEquals(3,v1.query().direction(OUT).labels("bought").has("time",Cmp.LESS_THAN,5).count());
        assertEquals(3,v1.query().direction(OUT).labels("bought").has("time",Cmp.GREATER_THAN,0).count());
        assertEquals(2,v1.query().direction(OUT).labels("bought").has("time",Cmp.LESS_THAN,3).count());
        assertEquals(1,v1.query().direction(OUT).labels("bought").has("time",Cmp.GREATER_THAN,2).count());
        assertEquals(2,v1.query().direction(OUT).labels("bought").hasNot("time").count());
        assertEquals(5,v1.query().direction(OUT).labels("bought").count());


        newTx();
        v1 = tx.getVertex(v1.longId());
        //Queries copied from above

        assertEquals(5,v1.query().direction(OUT).labels("bought").count());
        assertEquals(1,v1.query().direction(OUT).labels("bought").has("time",1).count());
        assertEquals(1,v1.query().direction(OUT).labels("bought").has("time",Cmp.LESS_THAN,3).has("time",Cmp.GREATER_THAN,1).count());
        assertEquals(3,v1.query().direction(OUT).labels("bought").has("time",Cmp.LESS_THAN,5).count());
        assertEquals(3,v1.query().direction(OUT).labels("bought").has("time",Cmp.GREATER_THAN,0).count());
        assertEquals(2,v1.query().direction(OUT).labels("bought").has("time",Cmp.LESS_THAN,3).count());
        assertEquals(1,v1.query().direction(OUT).labels("bought").has("time",Cmp.GREATER_THAN,2).count());
        assertEquals(2,v1.query().direction(OUT).labels("bought").hasNot("time").count());
        assertEquals(5,v1.query().direction(OUT).labels("bought").count());
    }

    //Add more removal operations, different transaction contexts
    @Test
    public void testCreateDelete() {
        makeKey("weight",Double.class);
        PropertyKey uid = makeVertexIndexedUniqueKey("uid",Integer.class);
        ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("knows")).sortKey(uid).sortOrder(Order.DESC).directed().make();
        mgmt.makeEdgeLabel("father").multiplicity(Multiplicity.MANY2ONE).make();
        finishSchema();

        TitanVertex v1 = graph.addVertex(), v3 = graph.addVertex("uid", 445);
        Edge e = v3.addEdge("knows", v1, "uid", 111);
        Edge e2 = v1.addEdge("friend",v3);
        assertEquals(111, e.<Integer>value("uid").intValue());
        graph.tx().commit();

        v3 = getV(graph,v3);
        assertEquals(445, v3.<Integer>value("uid").intValue());
        e = getOnlyElement(v3.query().direction(Direction.OUT).labels("knows").edges());
        assertEquals(111, e.<Integer>value("uid").intValue());
        assertEquals(e, getE(graph,e.id()));
        assertEquals(e, getE(graph,e.id().toString()));
        VertexProperty p = getOnlyElement(v3.properties("uid"));
        p.remove();
        v3.property("uid", 353);

        e = getOnlyElement(v3.query().direction(Direction.OUT).labels("knows").edges());
        e.property("uid",222);

        e2 = getOnlyElement(v1.query().direction(Direction.OUT).labels("friend").edges());
        e2.property("uid", 1);
        e2.property("weight", 2.0);

        assertEquals(1,e2.<Integer>value("uid").intValue());
        assertEquals(2.0, e2.<Double>value("weight").doubleValue(),0.0001);


        clopen();

        v3 = getV(graph,v3.id());
        assertEquals(353, v3.<Integer>value("uid").intValue());

        e = getOnlyElement(v3.query().direction(Direction.OUT).labels("knows").edges());
        assertEquals(222,e.<Integer>value("uid").intValue());
    }

   /* ==================================================================================
                            TIME TO LIVE
     ==================================================================================*/

    @Test
    public void testEdgeTTLTiming() throws Exception {
        if (!features.hasCellTTL()) {
            return;
        }

        EdgeLabel label1 = mgmt.makeEdgeLabel("likes").make();
        int ttl1 = 1;
        int ttl2 = 4;
        mgmt.setTTL(label1, ttl1, TimeUnit.SECONDS);
        EdgeLabel label2 = mgmt.makeEdgeLabel("dislikes").make();
        mgmt.setTTL(label2, ttl2, TimeUnit.SECONDS);
        EdgeLabel label3 = mgmt.makeEdgeLabel("indifferentTo").make();
        assertEquals(ttl1, mgmt.getTTL(label1).getLength(TimeUnit.SECONDS));
        assertEquals(ttl2, mgmt.getTTL(label2).getLength(TimeUnit.SECONDS));
        assertEquals(0, mgmt.getTTL(label3).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        TitanVertex v1 = graph.addVertex(), v2 = graph.addVertex(), v3 = graph.addVertex();

        v1.addEdge("likes",v2);
        v2.addEdge("dislikes", v1);
        v3.addEdge("indifferentTo", v1);

        // initial, pre-commit state of the edges.  They are not yet subject to TTL
        assertNotEmpty(v1.query().direction(Direction.OUT).vertices());
        assertNotEmpty(v2.query().direction(Direction.OUT).vertices());
        assertNotEmpty(v3.query().direction(Direction.OUT).vertices());

        long commitTime = System.currentTimeMillis();
        graph.tx().commit();

        // edges are now subject to TTL, although we must commit() or rollback() to see it
        assertNotEmpty(v1.query().direction(Direction.OUT).vertices());
        assertNotEmpty(v2.query().direction(Direction.OUT).vertices());
        assertNotEmpty(v3.query().direction(Direction.OUT).vertices());

        Thread.sleep(commitTime + (ttl1 * 1000L + 200) - System.currentTimeMillis());
        graph.tx().rollback();

        // e1 has dropped out
        assertEmpty(v1.query().direction(Direction.OUT).vertices());
        assertNotEmpty(v2.query().direction(Direction.OUT).vertices());
        assertNotEmpty(v3.query().direction(Direction.OUT).vertices());

        Thread.sleep(commitTime + (ttl2 * 1000L + 500) - System.currentTimeMillis());
        graph.tx().rollback();

        // both e1 and e2 have dropped out.  e3 has no TTL, and so remains
        assertEmpty(v1.query().direction(Direction.OUT).vertices());
        assertEmpty(v2.query().direction(Direction.OUT).vertices());
        assertNotEmpty(v3.query().direction(Direction.OUT).vertices());
    }

    @Test
    public void testEdgeTTLWithTransactions() throws Exception {
        if (!features.hasCellTTL()) {
            return;
        }

        EdgeLabel label1 = mgmt.makeEdgeLabel("likes").make();
        mgmt.setTTL(label1, 1, TimeUnit.SECONDS);
        assertEquals(1, mgmt.getTTL(label1).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        TitanVertex v1 = graph.addVertex(), v2 = graph.addVertex();

        v1.addEdge("likes",v2);

        // pre-commit state of the edge.  It is not yet subject to TTL
        assertNotEmpty(v1.query().direction(Direction.OUT).vertices());

        Thread.sleep(1001);

        // the edge should have expired by now, but only if it had been committed
        assertNotEmpty(v1.query().direction(Direction.OUT).vertices());

        graph.tx().commit();

        // still here, because we have just committed the edge.  Its countdown starts at the commit
        assertNotEmpty(v1.query().direction(Direction.OUT).vertices());

        Thread.sleep(1001);

        // the edge has expired in Cassandra, but still appears alive in this transaction
        assertNotEmpty(v1.query().direction(Direction.OUT).vertices());

        // syncing with the data store, we see that the edge has expired
        graph.tx().rollback();
        assertEmpty(v1.query().direction(Direction.OUT).vertices());
    }

    @Category({ BrittleTests.class })
    @Test
    public void testEdgeTTLWithIndex() throws Exception {
        if (!features.hasCellTTL()) {
            return;
        }

        int ttl = 1; // artificially low TTL for test
        final PropertyKey time = mgmt.makePropertyKey("time").dataType(Integer.class).make();
        EdgeLabel wavedAt = mgmt.makeEdgeLabel("wavedAt").signature(time).make();
        mgmt.buildEdgeIndex(wavedAt, "timeindex", Direction.BOTH, decr, time);
        mgmt.buildIndex("edge-time", Edge.class).addKey(time).buildCompositeIndex();
        mgmt.setTTL(wavedAt, ttl, TimeUnit.SECONDS);
        assertEquals(0, mgmt.getTTL(time).getLength(TimeUnit.SECONDS));
        assertEquals(ttl, mgmt.getTTL(wavedAt).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        TitanVertex v1 = graph.addVertex(), v2 = graph.addVertex();
        v1.addEdge("wavedAt",v2,"time", 42);

        assertTrue(v1.query().direction(Direction.OUT).interval("time", 0, 100).edges().iterator().hasNext());
        assertNotEmpty(v1.query().direction(Direction.OUT).edges());
        assertNotEmpty(graph.query().has("time",42).edges());

        graph.tx().commit();
        long commitTime = System.currentTimeMillis();

        assertTrue(v1.query().direction(Direction.OUT).interval("time", 0, 100).edges().iterator().hasNext());
        assertNotEmpty(v1.query().direction(Direction.OUT).edges());
        assertNotEmpty(graph.query().has("time",42).edges());

        Thread.sleep(commitTime + (ttl * 1000L + 100) - System.currentTimeMillis());
        graph.tx().rollback();

        assertFalse(v1.query().direction(Direction.OUT).interval("time", 0, 100).edges().iterator().hasNext());
        assertEmpty(v1.query().direction(Direction.OUT).edges());
        assertEmpty(graph.query().has("time",42).edges());
    }

    @Category({ BrittleTests.class })
    @Test
    public void testPropertyTTLTiming() throws Exception {
        if (!features.hasCellTTL()) {
            return;
        }

        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).make();
        PropertyKey place = mgmt.makePropertyKey("place").dataType(String.class).make();
        mgmt.setTTL(name, 42, TimeUnit.SECONDS);
        mgmt.setTTL(place, 1, TimeUnit.SECONDS);
        TitanGraphIndex index1 = mgmt.buildIndex("index1", Vertex.class).addKey(name).buildCompositeIndex();
        TitanGraphIndex index2 = mgmt.buildIndex("index2", Vertex.class).addKey(name).addKey(place).buildCompositeIndex();
        VertexLabel label1 = mgmt.makeVertexLabel("event").setStatic().make();
        mgmt.setTTL(label1, 2, TimeUnit.SECONDS);
        assertEquals(42, mgmt.getTTL(name).getLength(TimeUnit.SECONDS));
        assertEquals(1, mgmt.getTTL(place).getLength(TimeUnit.SECONDS));
        assertEquals(2, mgmt.getTTL(label1).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        TitanVertex v1 = tx.addVertex(LABEL_NAME,"event","name", "some event","place", "somewhere");

        tx.commit();
        Object id = v1.id();

        v1 = getV(graph,id);
        assertNotNull(v1);
        assertNotEmpty(graph.query().has("name","some event").has("place","somewhere").vertices());
        assertNotEmpty(graph.query().has("name","some event").vertices());

        Thread.sleep(1001);
        graph.tx().rollback();

        // short-lived property expires first
        v1 = getV(graph,id);
        assertNotNull(v1);
        assertEmpty(graph.query().has("name","some event").has("place","somewhere").vertices());
        assertNotEmpty(graph.query().has("name","some event").vertices());

        Thread.sleep(1001);
        graph.tx().rollback();

        // vertex expires before defined TTL of the long-lived property
        assertEmpty(graph.query().has("name","some event").has("place","somewhere").vertices());
        assertEmpty(graph.query().has("name","some event").vertices());
        v1 = getV(graph,id);
        assertNull(v1);
    }

    @Test
    public void testVertexTTLWithCompositeIndex() throws Exception {
        if (!features.hasCellTTL()) {
            return;
        }

        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).make();
        PropertyKey time = mgmt.makePropertyKey("time").dataType(Long.class).make();
        TitanGraphIndex index1 = mgmt.buildIndex("index1", Vertex.class).addKey(name).buildCompositeIndex();
        TitanGraphIndex index2 = mgmt.buildIndex("index2", Vertex.class).addKey(name).addKey(time).buildCompositeIndex();
        VertexLabel label1 = mgmt.makeVertexLabel("event").setStatic().make();
        mgmt.setTTL(label1, 1, TimeUnit.SECONDS);
        assertEquals(0, mgmt.getTTL(name).getLength(TimeUnit.SECONDS));
        assertEquals(0, mgmt.getTTL(time).getLength(TimeUnit.SECONDS));
        assertEquals(1, mgmt.getTTL(label1).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        TitanVertex v1 = tx.addVertex(LABEL_NAME, "event","name", "some event","time", System.currentTimeMillis());
        tx.commit();
        Object id = v1.id();

        v1 = getV(graph,id);
        assertNotNull(v1);
        assertNotEmpty(graph.query().has("name", "some event").vertices());

        Thread.sleep(1001);
        graph.tx().rollback();

        v1 = getV(graph,id);
        assertNull(v1);
        assertEmpty(graph.query().has("name", "some event").vertices());
    }

    @Category({ BrittleTests.class })
    @Test
    public void testEdgeTTLLimitedByVertexTTL() throws Exception {

        if (!features.hasCellTTL()) {
            return;
        }
        Boolean dbCache = config.get("cache.db-cache", Boolean.class);
        if (null == dbCache) {
            dbCache = false;
        }

        EdgeLabel likes = mgmt.makeEdgeLabel("likes").make();
        mgmt.setTTL(likes, 42, TimeUnit.SECONDS); // long edge TTL will be overridden by short vertex TTL
        EdgeLabel dislikes = mgmt.makeEdgeLabel("dislikes").make();
        mgmt.setTTL(dislikes, 1, TimeUnit.SECONDS);
        EdgeLabel indifferentTo = mgmt.makeEdgeLabel("indifferentTo").make();
        VertexLabel label1 = mgmt.makeVertexLabel("person").setStatic().make();
        mgmt.setTTL(label1, 2, TimeUnit.SECONDS);
        assertEquals(42, mgmt.getTTL(likes).getLength(TimeUnit.SECONDS));
        assertEquals(1, mgmt.getTTL(dislikes).getLength(TimeUnit.SECONDS));
        assertEquals(0, mgmt.getTTL(indifferentTo).getLength(TimeUnit.SECONDS));
        assertEquals(2, mgmt.getTTL(label1).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        TitanVertex v1 = tx.addVertex("person");
        TitanVertex v2 = tx.addVertex();
        Edge v1LikesV2 = v1.addEdge("likes",v2);
        Edge v1DislikesV2 = v1.addEdge("dislikes",v2);
        Edge v1IndifferentToV2 = v1.addEdge("indifferentTo",v2);
        tx.commit();
        long commitTime = System.currentTimeMillis();

        Object v1Id = v1.id();
        Object v2id = v2.id();
        Object v1LikesV2Id = v1LikesV2.id();
        Object v1DislikesV2Id = v1DislikesV2.id();
        Object v1IndifferentToV2Id = v1IndifferentToV2.id();

        v1 = getV(graph,v1Id);
        v2 = getV(graph,v2id);
        v1LikesV2 = getE(graph,v1LikesV2Id);
        v1DislikesV2 = getE(graph,v1DislikesV2Id);
        v1IndifferentToV2 = getE(graph,v1IndifferentToV2Id);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v1LikesV2);
        assertNotNull(v1DislikesV2);
        assertNotNull(v1IndifferentToV2);
        assertNotEmpty(v2.query().direction(Direction.IN).labels("likes").edges());
        assertNotEmpty(v2.query().direction(Direction.IN).labels("dislikes").edges());
        assertNotEmpty(v2.query().direction(Direction.IN).labels("indifferentTo").edges());

        Thread.sleep(commitTime + 1001L - System.currentTimeMillis());
        graph.tx().rollback();

        v1 = getV(graph,v1Id);
        v2 = getV(graph,v2id);
        v1LikesV2 = getE(graph,v1LikesV2Id);
        v1DislikesV2 = getE(graph,v1DislikesV2Id);
        v1IndifferentToV2 = getE(graph,v1IndifferentToV2Id);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v1LikesV2);
        // this edge has expired
        assertNull(v1DislikesV2);
        assertNotNull(v1IndifferentToV2);
        assertNotEmpty(v2.query().direction(Direction.IN).labels("likes").edges());
        // expired
        assertEmpty(v2.query().direction(Direction.IN).labels("dislikes").edges());
        assertNotEmpty(v2.query().direction(Direction.IN).labels("indifferentTo").edges());

        Thread.sleep(commitTime + 2001L - System.currentTimeMillis());
        graph.tx().rollback();

        v1 = getV(graph,v1Id);
        v2 = getV(graph,v2id);
        v1LikesV2 = getE(graph,v1LikesV2Id);
        v1DislikesV2 = getE(graph,v1DislikesV2Id);
        v1IndifferentToV2 = getE(graph,v1IndifferentToV2Id);
        // the vertex itself has expired
        assertNull(v1);
        assertNotNull(v2);
        // all incident edges have necessarily expired
        assertNull(v1LikesV2);
        assertNull(v1DislikesV2);
        assertNull(v1IndifferentToV2);

        if (dbCache) {
            /* TODO: uncomment
            assertNotEmpty(v2.query().direction(Direction.IN).labels("likes").edges());
            assertNotEmpty(v2.query().direction(Direction.IN).labels("dislikes").edges());
            assertNotEmpty(v2.query().direction(Direction.IN).labels("indifferentTo").edges());
            */
        } else {
            assertEmpty(v2.query().direction(Direction.IN).labels("likes").edges());
            assertEmpty(v2.query().direction(Direction.IN).labels("dislikes").edges());
            assertEmpty(v2.query().direction(Direction.IN).labels("indifferentTo").edges());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSettingTTLOnUnsupportedType() throws Exception {
        if (!features.hasCellTTL()) {
            throw new IllegalArgumentException();
        }

        TitanSchemaType type = ImplicitKey.ID;
        mgmt.setTTL(type, 0, TimeUnit.SECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTTLFromUnsupportedType() throws Exception {
        if (!features.hasCellTTL()) {
            throw new IllegalArgumentException();
        }

        TitanSchemaType type = ImplicitKey.ID;
        mgmt.getTTL(type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSettingTTLOnNonStaticVertexLabel() throws Exception {
        if (!features.hasCellTTL()) {
            throw new IllegalArgumentException();
        }

        VertexLabel label1 = mgmt.makeVertexLabel("event").make();
        mgmt.setTTL(label1, 42, TimeUnit.SECONDS);
    }

    @Test
    public void testEdgeTTLImplicitKey() throws Exception {
        Duration d;

        if (!features.hasCellTTL()) {
            return;
        }

        clopen(option(GraphDatabaseConfiguration.STORE_META_TTL, "edgestore"), true);

        assertEquals("~ttl", ImplicitKey.TTL.name());

        int ttl = 24*60*60;
        EdgeLabel likes = mgmt.makeEdgeLabel("likes").make();
        EdgeLabel hasLiked = mgmt.makeEdgeLabel("hasLiked").make();
        mgmt.setTTL(likes, ttl, TimeUnit.SECONDS);
        assertEquals(ttl, mgmt.getTTL(likes).getLength(TimeUnit.SECONDS));
        assertEquals(0, mgmt.getTTL(hasLiked).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        TitanVertex v1 = graph.addVertex(), v2 = graph.addVertex();

        Edge e1 = v1.addEdge("likes",v2);
        Edge e2 = v1.addEdge("hasLiked",v2);
        graph.tx().commit();

        // read from the edge created in this transaction
        d = e1.value("~ttl");
        assertEquals(86400, d.getLength(TimeUnit.SECONDS));

        // get the edge via a vertex
        e1 = getOnlyElement(v1.query().direction(Direction.OUT).labels("likes").edges());
        d = e1.value("~ttl");
        assertEquals(86400, d.getLength(TimeUnit.SECONDS));

        // returned value of ^ttl is the total time to live since commit, not remaining time
        Thread.sleep(1001);
        graph.tx().rollback();
        e1 = getOnlyElement(v1.query().direction(Direction.OUT).labels("likes").edges());
        d = e1.value("~ttl");
        assertEquals(86400, d.getLength(TimeUnit.SECONDS));

        // no ttl on edges of this label
        d = e2.value("~ttl");
        assertEquals(0, d.getLength(TimeUnit.SECONDS));
    }

    @Test
    public void testVertexTTLImplicitKey() throws Exception {
        Duration d;

        if (!features.hasCellTTL()) {
            return;
        }

        clopen(option(GraphDatabaseConfiguration.STORE_META_TTL, "edgestore"), true);

        int ttl1 = 1;
        VertexLabel label1 = mgmt.makeVertexLabel("event").setStatic().make();
        mgmt.setTTL(label1, ttl1, TimeUnit.SECONDS);
        assertEquals(ttl1, mgmt.getTTL(label1).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        TitanVertex v1 = tx.addVertex("event");
        TitanVertex v2 = tx.addVertex();
        tx.commit();

        /* TODO: this fails
        d = v1.getProperty("~ttl");
        assertEquals(1, d.getLength(TimeUnit.SECONDS));
        d = v2.getProperty("~ttl");
        assertEquals(0, d.getLength(TimeUnit.SECONDS));
        */

        Object v1id = v1.id();
        Object v2id = v2.id();
        v1 = getV(graph,v1id);
        v2 = getV(graph,v2id);

        d = v1.value("~ttl");
        assertEquals(1, d.getLength(TimeUnit.SECONDS));
        d = v2.value("~ttl");
        assertEquals(0, d.getLength(TimeUnit.SECONDS));
    }
}