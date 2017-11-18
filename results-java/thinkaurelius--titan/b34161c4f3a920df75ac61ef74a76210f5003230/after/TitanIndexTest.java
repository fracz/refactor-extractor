package com.thinkaurelius.titan.graphdb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.attribute.*;
import com.thinkaurelius.titan.core.schema.Mapping;
import com.thinkaurelius.titan.core.schema.Parameter;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.diskstorage.Backend;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.example.GraphOfTheGodsFactory;
import com.thinkaurelius.titan.graphdb.internal.ElementCategory;
import com.thinkaurelius.titan.graphdb.types.StandardEdgeLabelMaker;
import com.thinkaurelius.titan.testutil.TestUtil;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.thinkaurelius.titan.graphdb.TitanGraphTest.evaluateQuery;
import static org.junit.Assert.*;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public abstract class TitanIndexTest extends TitanGraphBaseTest {

    public static final String INDEX = GraphOfTheGodsFactory.INDEX_NAME;
    public static final String VINDEX = "v"+INDEX;
    public static final String EINDEX = "e"+INDEX;
    public static final String PINDEX = "p"+INDEX;


    public final boolean supportsGeoPoint;
    public final boolean supportsNumeric;
    public final boolean supportsText;

    private static final Logger log =
            LoggerFactory.getLogger(TitanIndexTest.class);

    protected TitanIndexTest(boolean supportsGeoPoint, boolean supportsNumeric, boolean supportsText) {
        this.supportsGeoPoint = supportsGeoPoint;
        this.supportsNumeric = supportsNumeric;
        this.supportsText = supportsText;
    }

    public abstract boolean supportsLuceneStyleQueries();

    @Rule
    public TestName methodName = new TestName();

    /**
     * Tests the {@link com.thinkaurelius.titan.example.GraphOfTheGodsFactory#load(com.thinkaurelius.titan.core.TitanGraph)}
     * method used as the standard example that ships with Titan.
     */
    @Test
    public void testGraphOfTheGods() {
        GraphOfTheGodsFactory.load(graph);

        assertEquals(12,Iterables.size(graph.getVertices()));
        assertEquals(3,Iterables.size(graph.getVertices("label","god")));
        Vertex h = Iterables.getOnlyElement(graph.getVertices("name","hercules"));
        assertEquals(30,h.getProperty("age"));
        assertEquals("demigod",((TitanVertex)h).getLabel());
        assertEquals(5,Iterables.size(h.getEdges(Direction.BOTH)));
    }

    @Test
    public void testSimpleUpdate() {
        PropertyKey text = makeKey("name",String.class);
        EdgeLabel knows = makeLabel("knows");
        mgmt.buildIndex("namev",Vertex.class).indexKey(text).buildCompositeIndex();
        mgmt.buildIndex("namee",Edge.class).indexKey(text).buildCompositeIndex();
        finishSchema();

        Vertex v = tx.addVertex();
        v.setProperty("name", "Marko Rodriguez");
        Edge e = v.addEdge("knows",v);
        e.setProperty("name","Hulu Bubab");
        assertEquals(1, Iterables.size(tx.query().has("name", Text.CONTAINS, "marko").vertices()));
        assertEquals(1, Iterables.size(tx.query().has("name", Text.CONTAINS, "Hulu").edges()));
        for (Vertex u : tx.getVertices()) assertEquals("Marko Rodriguez",u.getProperty("name"));
        clopen();
        assertEquals(1, Iterables.size(tx.query().has("name", Text.CONTAINS, "marko").vertices()));
        assertEquals(1, Iterables.size(tx.query().has("name", Text.CONTAINS, "Hulu").edges()));
        for (Vertex u : tx.getVertices()) assertEquals("Marko Rodriguez",u.getProperty("name"));
        v = (Vertex) Iterables.getOnlyElement(tx.query().has("name", Text.CONTAINS, "marko").vertices());
        v.setProperty("name", "Marko");
        e = Iterables.getOnlyElement(v.getEdges(Direction.OUT));
        e.setProperty("name","Tubu Rubu");
        assertEquals(1, Iterables.size(tx.query().has("name", Text.CONTAINS, "marko").vertices()));
        assertEquals(1, Iterables.size(tx.query().has("name", Text.CONTAINS, "Rubu").edges()));
        for (Vertex u : tx.getVertices()) assertEquals("Marko",u.getProperty("name"));
        newTx();
        assertEquals(1, Iterables.size(tx.query().has("name", Text.CONTAINS, "marko").vertices()));
        assertEquals(1, Iterables.size(tx.query().has("name", Text.CONTAINS, "Rubu").edges()));
        for (Vertex u : tx.getVertices()) assertEquals("Marko",u.getProperty("name"));
    }

    @Test
    public void testIndexing() {

        PropertyKey text = makeKey("text",String.class);
        createExternalVertexIndex(text,INDEX);
        createExternalEdgeIndex(text,INDEX);

        PropertyKey location = makeKey("location",Geoshape.class);
        createExternalVertexIndex(location,INDEX);
        createExternalEdgeIndex(location,INDEX);

        PropertyKey time = makeKey("time",Long.class);
        createExternalVertexIndex(time,INDEX);
        createExternalEdgeIndex(time,INDEX);

        PropertyKey category = makeKey("category",Integer.class);
        mgmt.buildIndex("vcategory",Vertex.class).indexKey(category).buildCompositeIndex();
        mgmt.buildIndex("ecategory",Edge.class).indexKey(category).buildCompositeIndex();

        PropertyKey group = makeKey("group",Byte.class);
        createExternalVertexIndex(group,INDEX);
        createExternalEdgeIndex(group,INDEX);

        PropertyKey id = makeVertexIndexedKey("uid",Integer.class);
        EdgeLabel knows = ((StandardEdgeLabelMaker)mgmt.makeEdgeLabel("knows")).sortKey(time).signature(location).make();
        finishSchema();

        clopen();
        String[] words = {"world", "aurelius", "titan", "graph"};
        int numCategories = 5;
        int numGroups = 10;
        double distance, offset;
        int numV = 100;
        final int originalNumV = numV;
        for (int i = 0; i < numV; i++) {
            Vertex v = tx.addVertex();
            v.setProperty("uid", i);
            v.setProperty("category", i % numCategories);
            v.setProperty("group", i % numGroups);
            v.setProperty("text", "Vertex " + words[i % words.length]);
            v.setProperty("time", i);
            offset = (i % 2 == 0 ? 1 : -1) * (i * 50.0 / numV);
            v.setProperty("location", Geoshape.point(0.0 + offset, 0.0 + offset));

            Edge e = v.addEdge("knows", getVertex("uid", Math.max(0, i - 1)));
            e.setProperty("text", "Vertex " + words[i % words.length]);
            e.setProperty("time", i);
            e.setProperty("category", i % numCategories);
            e.setProperty("group", i % numGroups);
            e.setProperty("location", Geoshape.point(0.0 + offset, 0.0 + offset));
        }

        for (int i = 0; i < words.length; i++) {
            int expectedSize = numV / words.length;
            assertEquals(expectedSize, Iterables.size(tx.query().has("text", Text.CONTAINS, words[i]).vertices()));
            assertEquals(expectedSize, Iterables.size(tx.query().has("text", Text.CONTAINS, words[i]).edges()));

            //Test ordering
            for (String orderKey : new String[]{"time", "category"}) {
                for (Order order : Order.values()) {
                    for (Iterable<? extends Element> iter : ImmutableList.of(
                            tx.query().has("text", Text.CONTAINS, words[i]).orderBy(orderKey, order).vertices(),
                            tx.query().has("text", Text.CONTAINS, words[i]).orderBy(orderKey, order).edges()
                    )) {
                        Element previous = null;
                        int count = 0;
                        for (Element element : iter) {
                            if (previous != null) {
                                int cmp = ((Comparable) element.getProperty(orderKey)).compareTo(previous.getProperty(orderKey));
                                assertTrue(element.getProperty(orderKey) + " <> " + previous.getProperty(orderKey),
                                        order == Order.ASC ? cmp >= 0 : cmp <= 0);
                            }
                            previous = element;
                            count++;
                        }
                        assertEquals(expectedSize, count);
                    }
                }
            }
        }

        assertEquals(3, Iterables.size(tx.query().has("group", 3).orderBy("time", Order.ASC).limit(3).vertices()));
        assertEquals(3, Iterables.size(tx.query().has("group", 3).orderBy("time", Order.DESC).limit(3).edges()));

        for (int i = 0; i < numV / 2; i += numV / 10) {
            assertEquals(i, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, i).has("time", Cmp.LESS_THAN, i + i).vertices()));
            assertEquals(i, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, i).has("time", Cmp.LESS_THAN, i + i).edges()));
        }

        for (int i = 0; i < numV; i += 10) {
            offset = (i * 50.0 / originalNumV);
            distance = Geoshape.point(0.0, 0.0).getPoint().distance(Geoshape.point(offset, offset).getPoint()) + 20;
            assertEquals(i + 1, Iterables.size(tx.query().has("location", Geo.WITHIN, Geoshape.circle(0.0, 0.0, distance)).vertices()));
            assertEquals(i + 1, Iterables.size(tx.query().has("location", Geo.WITHIN, Geoshape.circle(0.0, 0.0, distance)).edges()));
        }

        //Mixed index queries
        assertEquals(4, Iterables.size(tx.query().has("category", 1).interval("time", 10, 28).vertices()));
        assertEquals(4, Iterables.size(tx.query().has("category", 1).interval("time", 10, 28).edges()));

        assertEquals(5, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, 10).has("time", Cmp.LESS_THAN, 30).has("text", Text.CONTAINS, words[0]).vertices()));
        offset = (19 * 50.0 / originalNumV);
        distance = Geoshape.point(0.0, 0.0).getPoint().distance(Geoshape.point(offset, offset).getPoint()) + 20;
        assertEquals(5, Iterables.size(tx.query().has("location", Geo.INTERSECT, Geoshape.circle(0.0, 0.0, distance)).has("text", Text.CONTAINS, words[0]).vertices()));

        assertEquals(numV, Iterables.size(tx.getVertices()));
        assertEquals(numV, Iterables.size(tx.getEdges()));

        //--------------

        clopen();

        //##########################
        //Copied from above
        //##########################

        for (int i = 0; i < words.length; i++) {
            int expectedSize = numV / words.length;
            assertEquals(expectedSize, Iterables.size(tx.query().has("text", Text.CONTAINS, words[i]).vertices()));
            assertEquals(expectedSize, Iterables.size(tx.query().has("text", Text.CONTAINS, words[i]).edges()));

            //Test ordering
            for (String orderKey : new String[]{"time", "category"}) {
                for (Order order : Order.values()) {
                    for (Iterable<? extends Element> iter : ImmutableList.of(
                            tx.query().has("text", Text.CONTAINS, words[i]).orderBy(orderKey, order).vertices(),
                            tx.query().has("text", Text.CONTAINS, words[i]).orderBy(orderKey, order).edges()
                    )) {
                        TestUtil.verifyElementOrder(iter,orderKey,order,expectedSize);
                    }
                }
            }
        }

        assertEquals(3, Iterables.size(tx.query().has("group", 3).orderBy("time", Order.ASC).limit(3).vertices()));
        assertEquals(3, Iterables.size(tx.query().has("group", 3).orderBy("time", Order.DESC).limit(3).edges()));

        for (int i = 0; i < numV / 2; i += numV / 10) {
            assertEquals(i, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, i).has("time", Cmp.LESS_THAN, i + i).vertices()));
            assertEquals(i, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, i).has("time", Cmp.LESS_THAN, i + i).edges()));
        }

        for (int i = 0; i < numV; i += 10) {
            offset = (i * 50.0 / originalNumV);
            distance = Geoshape.point(0.0, 0.0).getPoint().distance(Geoshape.point(offset, offset).getPoint()) + 20;
            assertEquals(i + 1, Iterables.size(tx.query().has("location", Geo.WITHIN, Geoshape.circle(0.0, 0.0, distance)).vertices()));
            assertEquals(i + 1, Iterables.size(tx.query().has("location", Geo.WITHIN, Geoshape.circle(0.0, 0.0, distance)).edges()));
        }

        //Mixed index queries
        assertEquals(4, Iterables.size(tx.query().has("category", 1).interval("time", 10, 28).vertices()));
        assertEquals(4, Iterables.size(tx.query().has("category", 1).interval("time", 10, 28).edges()));

        assertEquals(5, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, 10).has("time", Cmp.LESS_THAN, 30).has("text", Text.CONTAINS, words[0]).vertices()));
        offset = (19 * 50.0 / originalNumV);
        distance = Geoshape.point(0.0, 0.0).getPoint().distance(Geoshape.point(offset, offset).getPoint()) + 20;
        assertEquals(5, Iterables.size(tx.query().has("location", Geo.INTERSECT, Geoshape.circle(0.0, 0.0, distance)).has("text", Text.CONTAINS, words[0]).vertices()));

        assertEquals(numV, Iterables.size(tx.getVertices()));
        assertEquals(numV, Iterables.size(tx.getEdges()));

        newTx();

        int numDelete = 12;
        for (int i = numV - numDelete; i < numV; i++) {
            getVertex("uid", i).remove();
        }

        numV = numV - numDelete;

        //Copied from above
        for (int i = 0; i < words.length; i++) {
            assertEquals(numV / words.length, Iterables.size(tx.query().has("text", Text.CONTAINS, words[i]).vertices()));
            assertEquals(numV / words.length, Iterables.size(tx.query().has("text", Text.CONTAINS, words[i]).edges()));
        }

        for (int i = 0; i < numV / 2; i += numV / 10) {
            assertEquals(i, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, i).has("time", Cmp.LESS_THAN, i + i).vertices()));
            assertEquals(i, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, i).has("time", Cmp.LESS_THAN, i + i).edges()));
        }

        for (int i = 0; i < numV; i += 10) {
            offset = (i * 50.0 / originalNumV);
            distance = Geoshape.point(0.0, 0.0).getPoint().distance(Geoshape.point(offset, offset).getPoint()) + 20;
            assertEquals(i + 1, Iterables.size(tx.query().has("location", Geo.WITHIN, Geoshape.circle(0.0, 0.0, distance)).vertices()));
            assertEquals(i + 1, Iterables.size(tx.query().has("location", Geo.WITHIN, Geoshape.circle(0.0, 0.0, distance)).edges()));
        }

        assertEquals(5, Iterables.size(tx.query().has("time", Cmp.GREATER_THAN_EQUAL, 10).has("time", Cmp.LESS_THAN, 30).has("text", Text.CONTAINS, words[0]).vertices()));
        offset = (19 * 50.0 / originalNumV);
        distance = Geoshape.point(0.0, 0.0).getPoint().distance(Geoshape.point(offset, offset).getPoint()) + 20;
        assertEquals(5, Iterables.size(tx.query().has("location", Geo.INTERSECT, Geoshape.circle(0.0, 0.0, distance)).has("text", Text.CONTAINS, words[0]).vertices()));

        assertEquals(numV, Iterables.size(tx.getVertices()));
        assertEquals(numV, Iterables.size(tx.getEdges()));

    }

    /**
     * Tests conditional indexing and the different management features
     */
    @Test
    public void testConditionalIndexing() {
        PropertyKey name = makeKey("name", String.class);
        PropertyKey weight = makeKey("weight",Decimal.class);
        PropertyKey text = makeKey("text", String.class);

        VertexLabel person = mgmt.makeVertexLabel("person").make();
        VertexLabel org = mgmt.makeVertexLabel("org").make();

        TitanGraphIndex index1 = mgmt.buildIndex("index1",Vertex.class).
                indexKey(name,Mapping.STRING.getParameter()).buildMixedIndex(INDEX);
        TitanGraphIndex index2 = mgmt.buildIndex("index2",Vertex.class).indexOnly(person).
                indexKey(text,Mapping.TEXT.getParameter()).indexKey(weight).buildMixedIndex(INDEX);
        TitanGraphIndex index3 = mgmt.buildIndex("index3",Vertex.class).indexOnly(org).
                indexKey(text,Mapping.TEXT.getParameter()).indexKey(weight).buildMixedIndex(INDEX);

        // ########### INSPECTION & FAILURE ##############
        assertTrue(mgmt.containsGraphIndex("index1"));
        assertFalse(mgmt.containsGraphIndex("index"));
        assertEquals(3,Iterables.size(mgmt.getGraphIndexes(Vertex.class)));
        assertNull(mgmt.getGraphIndex("indexx"));

        name = mgmt.getPropertyKey("name");
        weight = mgmt.getPropertyKey("weight");
        text = mgmt.getPropertyKey("text");
        person = mgmt.getVertexLabel("person");
        org = mgmt.getVertexLabel("org");
        index1 = mgmt.getGraphIndex("index1");
        index2 = mgmt.getGraphIndex("index2");
        index3 = mgmt.getGraphIndex("index3");

        assertTrue(Vertex.class.isAssignableFrom(index1.getIndexedElement()));
        assertEquals("index2",index2.getName());
        assertEquals(INDEX,index3.getBackingIndex());
        assertFalse(index2.isUnique());
        assertEquals(2,index3.getFieldKeys().length);
        assertEquals(1,index1.getFieldKeys().length);
        assertEquals(2,index3.getParametersFor(text).length);
        assertEquals(1,index3.getParametersFor(weight).length);

        try {
            //Already exists
            mgmt.buildIndex("index2",Vertex.class).indexKey(weight).buildMixedIndex(INDEX);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Already exists
            mgmt.buildIndex("index2",Vertex.class).indexKey(weight).buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Key is already added
            mgmt.addIndexKey(index2,weight);
            fail();
        } catch (IllegalArgumentException e) {}

        finishSchema();
        clopen();

        // ########### INSPECTION & FAILURE (copied from above) ##############
        assertTrue(mgmt.containsGraphIndex("index1"));
        assertFalse(mgmt.containsGraphIndex("index"));
        assertEquals(3,Iterables.size(mgmt.getGraphIndexes(Vertex.class)));
        assertNull(mgmt.getGraphIndex("indexx"));

        name = mgmt.getPropertyKey("name");
        weight = mgmt.getPropertyKey("weight");
        text = mgmt.getPropertyKey("text");
        person = mgmt.getVertexLabel("person");
        org = mgmt.getVertexLabel("org");
        index1 = mgmt.getGraphIndex("index1");
        index2 = mgmt.getGraphIndex("index2");
        index3 = mgmt.getGraphIndex("index3");

        assertTrue(Vertex.class.isAssignableFrom(index1.getIndexedElement()));
        assertEquals("index2",index2.getName());
        assertEquals(INDEX,index3.getBackingIndex());
        assertFalse(index2.isUnique());
        assertEquals(2,index3.getFieldKeys().length);
        assertEquals(1,index1.getFieldKeys().length);
        assertEquals(2,index3.getParametersFor(text).length);
        assertEquals(1,index3.getParametersFor(weight).length);

        try {
            //Already exists
            mgmt.buildIndex("index2",Vertex.class).indexKey(weight).buildMixedIndex(INDEX);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Already exists
            mgmt.buildIndex("index2",Vertex.class).indexKey(weight).buildCompositeIndex();
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            //Key is already added
            mgmt.addIndexKey(index2,weight);
            fail();
        } catch (IllegalArgumentException e) {}


        // ########### TRANSACTIONAL ##############
        name = tx.getPropertyKey("name");
        weight = tx.getPropertyKey("weight");
        text = tx.getPropertyKey("text");
        person = tx.getVertexLabel("person");
        org = tx.getVertexLabel("org");

        final int numV = 200;
        String[] strs = {"aaa","bbb","ccc","ddd"};
        String[] strs2= new String[strs.length];
        for (int i=0;i<strs.length;i++) strs2[i]=strs[i]+" "+strs[i];
        final int modulo = 5;
        assert numV%(modulo*strs.length*2)==0;

        for (int i=0;i<numV;i++) {
            TitanVertex v = tx.addVertex(i%2==0?person:org);
            v.addProperty(name,strs[i%strs.length]);
            v.addProperty(text,strs[i%strs.length]);
            v.addProperty(weight,(i%modulo)+0.5);
        }

        //########## QUERIES ################
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},index2.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).has("label",Cmp.EQUAL,person.getName()).orderBy(weight,Order.DESC),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},weight,Order.DESC,index2.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[3]).has("label",Cmp.EQUAL,org.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},index3.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[1]).has("label",Cmp.EQUAL,org.getName()).orderBy(weight,Order.DESC),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},weight,Order.DESC,index3.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).has(weight,Cmp.EQUAL,2.5).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                numV/(modulo*strs.length),new boolean[]{true,true},index2.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[2]).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,true},index1.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[3]).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                0,new boolean[]{false,true},index1.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},index1.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[2]).has(text,Text.CONTAINS,strs[2]).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},index1.getName(),index2.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]).has(text,Text.CONTAINS,strs[0]).has("label",Cmp.EQUAL,person.getName()).orderBy(weight,Order.ASC),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},weight,Order.ASC,index1.getName(),index2.getName());

        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,true});
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).orderBy(weight,Order.ASC),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,false},weight,Order.ASC);

        clopen();

        //########## QUERIES (copied from above) ################
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},index2.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).has("label",Cmp.EQUAL,person.getName()).orderBy(weight,Order.DESC),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},weight,Order.DESC,index2.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[3]).has("label",Cmp.EQUAL,org.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},index3.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[1]).has("label",Cmp.EQUAL,org.getName()).orderBy(weight,Order.DESC),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},weight,Order.DESC,index3.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).has(weight,Cmp.EQUAL,2.5).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                numV/(modulo*strs.length),new boolean[]{true,true},index2.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[2]).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,true},index1.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[3]).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                0,new boolean[]{false,true},index1.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},index1.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[2]).has(text,Text.CONTAINS,strs[2]).has("label",Cmp.EQUAL,person.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},index1.getName(),index2.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]).has(text,Text.CONTAINS,strs[0]).has("label",Cmp.EQUAL,person.getName()).orderBy(weight,Order.ASC),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},weight,Order.ASC,index1.getName(),index2.getName());

        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,true});
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).orderBy(weight,Order.ASC),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,false},weight,Order.ASC);
    }

    @Test
    public void testInternalAndExternalIndexing() {
        PropertyKey name = makeKey("name", String.class);
        PropertyKey weight = makeKey("weight",Decimal.class);
        PropertyKey text = makeKey("text", String.class);
        PropertyKey flag = makeKey("flag",Boolean.class);

        TitanGraphIndex internal = mgmt.buildIndex("internal",Vertex.class).indexKey(name).indexKey(weight).buildCompositeIndex();
        TitanGraphIndex external = mgmt.buildIndex("external",Vertex.class).indexKey(weight).indexKey(text,Mapping.TEXT.getParameter()).buildMixedIndex(INDEX);
        external.getName(); internal.getName();
        finishSchema();

        name = tx.getPropertyKey("name");
        weight = tx.getPropertyKey("weight");
        text = tx.getPropertyKey("text");
        flag = tx.getPropertyKey("flag");

        final int numV = 100;
        String[] strs = {"aaa","bbb","ccc","ddd"};
        String[] strs2= new String[strs.length];
        for (int i=0;i<strs.length;i++) strs2[i]=strs[i]+" "+strs[i];
        final int modulo = 5;
        final int divisor = modulo*strs.length;

        for (int i=0;i<numV;i++) {
            TitanVertex v = tx.addVertex();
            v.addProperty(name,strs[i%strs.length]);
            v.addProperty(text,strs[i%strs.length]);
            v.addProperty(weight,(i%modulo)+0.5);
            v.addProperty(flag,true);
        }

        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,true});
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},external.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).has(flag.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,true},external.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]).has(weight,Cmp.EQUAL,1.5),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{true,true},internal.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]).has(weight,Cmp.EQUAL,1.5).has(flag.getName()),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{false,true},internal.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[2]).has(weight,Cmp.EQUAL,2.5),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{true,true},external.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[2]).has(weight,Cmp.EQUAL,2.5).has(flag.getName()),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{false,true},external.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[3]).has(name,Cmp.EQUAL,strs[3]).has(weight,Cmp.EQUAL,3.5),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{true,true},external.getName(),internal.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[3]).has(name,Cmp.EQUAL,strs[3]).has(weight,Cmp.EQUAL,3.5).has(flag.getName()),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{false,true},external.getName(),internal.getName());

        clopen();

        //Same queries as above
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,true});
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{true,true},external.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[0]).has(flag.getName()),ElementCategory.VERTEX,
                numV/strs.length,new boolean[]{false,true},external.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]).has(weight,Cmp.EQUAL,1.5),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{true,true},internal.getName());
        evaluateQuery(tx.query().has(name,Cmp.EQUAL,strs[0]).has(weight,Cmp.EQUAL,1.5).has(flag.getName()),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{false,true},internal.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[2]).has(weight,Cmp.EQUAL,2.5),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{true,true},external.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[2]).has(weight,Cmp.EQUAL,2.5).has(flag.getName()),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{false,true},external.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[3]).has(name,Cmp.EQUAL,strs[3]).has(weight,Cmp.EQUAL,3.5),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{true,true},external.getName(),internal.getName());
        evaluateQuery(tx.query().has(text,Text.CONTAINS,strs[3]).has(name,Cmp.EQUAL,strs[3]).has(weight,Cmp.EQUAL,3.5).has(flag.getName()),ElementCategory.VERTEX,
                numV/divisor,new boolean[]{false,true},external.getName(),internal.getName());

    }


    private void setupChainGraph(int numV, String[] strs) {
        TitanGraphIndex vindex = getExternalIndex(Vertex.class,INDEX);
        TitanGraphIndex eindex = getExternalIndex(Edge.class,INDEX);
        TitanGraphIndex pindex = getExternalIndex(TitanProperty.class,INDEX);
        PropertyKey name = makeKey("name",String.class);

        mgmt.addIndexKey(vindex, name, Mapping.STRING.getParameter(), Parameter.of("mapped-name","vstr"));
        mgmt.addIndexKey(eindex,name, Mapping.STRING.getParameter(), Parameter.of("mapped-name", "estr"));
        mgmt.addIndexKey(pindex,name, Mapping.STRING.getParameter(), Parameter.of("mapped-name", "pstr"));
        PropertyKey text = makeKey("text",String.class);
        mgmt.addIndexKey(vindex,text, Mapping.TEXT.getParameter(), Parameter.of("mapped-name","vtext"));
        mgmt.addIndexKey(eindex,text, Mapping.TEXT.getParameter(), Parameter.of("mapped-name","etext"));
        mgmt.addIndexKey(pindex,text, Mapping.TEXT.getParameter(), Parameter.of("mapped-name","ptext"));
        mgmt.makeEdgeLabel("knows").signature(name).make();
        mgmt.makePropertyKey("uid").dataType(String.class).signature(text).make();
        finishSchema();
        TitanVertex previous = null;
        for (int i=0;i<numV;i++) {
            TitanVertex v = graph.addVertex(null);
            v.setProperty("name",strs[i%strs.length]);
            v.setProperty("text",strs[i%strs.length]);
            TitanEdge e = v.addEdge("knows",previous==null?v:previous);
            e.setProperty("name",strs[i%strs.length]);
            e.setProperty("text",strs[i%strs.length]);
            TitanProperty p = v.addProperty("uid","v"+i);
            p.setProperty("name", strs[i % strs.length]);
            p.setProperty("text", strs[i % strs.length]);
            previous=v;
        }
    }

    /**
     * Tests index parameters (mapping and names) and various string predicates
     */
    @Test
    public void testIndexParameters() {
        int numV = 1000;
        String[] strs = {"Uncle Berry has a farm","and on his farm he has five ducks","ducks are beautiful animals","the sky is very blue today"};
        setupChainGraph(numV,strs);

        evaluateQuery(graph.query().has("text",Text.CONTAINS,"ducks"),
                ElementCategory.VERTEX,numV/strs.length*2,new boolean[]{true,true},VINDEX);
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"ducks").vertices()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"farm").vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS,"beautiful").vertices()));
        evaluateQuery(graph.query().has("text",Text.CONTAINS_PREFIX,"beauti"),
                ElementCategory.VERTEX,numV/strs.length,new boolean[]{true,true},VINDEX);
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS_REGEX,"be[r]+y").vertices()));
        assertEquals(0,Iterables.size(graph.query().has("text",Text.CONTAINS,"lolipop").vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).vertices()));
        assertEquals(numV/strs.length*(strs.length-1),Iterables.size(graph.query().has("name",Cmp.NOT_EQUAL,strs[2]).vertices()));
        assertEquals(0,Iterables.size(graph.query().has("name",Cmp.EQUAL,"farm").vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Text.PREFIX,"ducks").vertices()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("name",Text.REGEX,"(.*)ducks(.*)").vertices()));

        //Same queries for edges
        evaluateQuery(graph.query().has("text",Text.CONTAINS,"ducks"),
                ElementCategory.EDGE,numV/strs.length*2,new boolean[]{true,true},EINDEX);
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"ducks").edges()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"farm").edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS,"beautiful").edges()));
        evaluateQuery(graph.query().has("text",Text.CONTAINS_PREFIX,"beauti"),
                ElementCategory.EDGE,numV/strs.length,new boolean[]{true,true},EINDEX);
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS_REGEX,"be[r]+y").edges()));
        assertEquals(0,Iterables.size(graph.query().has("text",Text.CONTAINS,"lolipop").edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).edges()));
        assertEquals(numV/strs.length*(strs.length-1),Iterables.size(graph.query().has("name",Cmp.NOT_EQUAL,strs[2]).edges()));
        assertEquals(0,Iterables.size(graph.query().has("name",Cmp.EQUAL,"farm").edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Text.PREFIX,"ducks").edges()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("name",Text.REGEX,"(.*)ducks(.*)").edges()));

        //Same queries for properties
        evaluateQuery(graph.query().has("text",Text.CONTAINS,"ducks"),
                ElementCategory.PROPERTY,numV/strs.length*2,new boolean[]{true,true},PINDEX);
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"ducks").properties()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"farm").properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS,"beautiful").properties()));
        evaluateQuery(graph.query().has("text",Text.CONTAINS_PREFIX,"beauti"),
                ElementCategory.PROPERTY,numV/strs.length,new boolean[]{true,true},PINDEX);
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS_REGEX,"be[r]+y").properties()));
        assertEquals(0,Iterables.size(graph.query().has("text",Text.CONTAINS,"lolipop").properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).properties()));
        assertEquals(numV/strs.length*(strs.length-1),Iterables.size(graph.query().has("label","uid").has("name",Cmp.NOT_EQUAL,strs[2]).properties()));
        assertEquals(0,Iterables.size(graph.query().has("name",Cmp.EQUAL,"farm").properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Text.PREFIX,"ducks").properties()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("name",Text.REGEX,"(.*)ducks(.*)").properties()));


        clopen();
        /* =======================================
        Same queries as above but against backend */

        evaluateQuery(graph.query().has("text",Text.CONTAINS,"ducks"),
                ElementCategory.VERTEX,numV/strs.length*2,new boolean[]{true,true},VINDEX);
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"ducks").vertices()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"farm").vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS,"beautiful").vertices()));
        evaluateQuery(graph.query().has("text",Text.CONTAINS_PREFIX,"beauti"),
                ElementCategory.VERTEX,numV/strs.length,new boolean[]{true,true},VINDEX);
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS_REGEX,"be[r]+y").vertices()));
        assertEquals(0,Iterables.size(graph.query().has("text",Text.CONTAINS,"lolipop").vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).vertices()));
        assertEquals(numV/strs.length*(strs.length-1),Iterables.size(graph.query().has("name",Cmp.NOT_EQUAL,strs[2]).vertices()));
        assertEquals(0,Iterables.size(graph.query().has("name",Cmp.EQUAL,"farm").vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Text.PREFIX,"ducks").vertices()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("name",Text.REGEX,"(.*)ducks(.*)").vertices()));

        //Same queries for edges
        evaluateQuery(graph.query().has("text",Text.CONTAINS,"ducks"),
                ElementCategory.EDGE,numV/strs.length*2,new boolean[]{true,true},EINDEX);
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"ducks").edges()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"farm").edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS,"beautiful").edges()));
        evaluateQuery(graph.query().has("text",Text.CONTAINS_PREFIX,"beauti"),
                ElementCategory.EDGE,numV/strs.length,new boolean[]{true,true},EINDEX);
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS_REGEX,"be[r]+y").edges()));
        assertEquals(0,Iterables.size(graph.query().has("text",Text.CONTAINS,"lolipop").edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).edges()));
        assertEquals(numV/strs.length*(strs.length-1),Iterables.size(graph.query().has("name",Cmp.NOT_EQUAL,strs[2]).edges()));
        assertEquals(0,Iterables.size(graph.query().has("name",Cmp.EQUAL,"farm").edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Text.PREFIX,"ducks").edges()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("name",Text.REGEX,"(.*)ducks(.*)").edges()));

        //Same queries for properties
        evaluateQuery(graph.query().has("text",Text.CONTAINS,"ducks"),
                ElementCategory.PROPERTY,numV/strs.length*2,new boolean[]{true,true},PINDEX);
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"ducks").properties()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("text",Text.CONTAINS,"farm").properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS,"beautiful").properties()));
        evaluateQuery(graph.query().has("text",Text.CONTAINS_PREFIX,"beauti"),
                ElementCategory.PROPERTY,numV/strs.length,new boolean[]{true,true},PINDEX);
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("text",Text.CONTAINS_REGEX,"be[r]+y").properties()));
        assertEquals(0,Iterables.size(graph.query().has("text",Text.CONTAINS,"lolipop").properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Cmp.EQUAL,strs[1]).properties()));
        assertEquals(numV/strs.length*(strs.length-1),Iterables.size(graph.query().has("label","uid").has("name",Cmp.NOT_EQUAL,strs[2]).properties()));
        assertEquals(0,Iterables.size(graph.query().has("name",Cmp.EQUAL,"farm").properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.query().has("name",Text.PREFIX,"ducks").properties()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.query().has("name",Text.REGEX,"(.*)ducks(.*)").properties()));

    }

    /**
     * Tests index parameters (mapping and names) with raw indexQuery
     */
    @Test
    public void testRawQueries() {
        if (!supportsLuceneStyleQueries()) return;

        int numV = 1000;
        String[] strs = {"Uncle Berry has a farm","and on his farm he has five ducks","ducks are beautiful animals","the sky is very blue today"};
        setupChainGraph(numV,strs);
        clopen();

        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(VINDEX,"v.text:ducks").vertices()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(VINDEX,"v.text:(farm uncle berry)").vertices()));
        assertEquals(numV/strs.length,Iterables.size(graph.indexQuery(VINDEX,"v.text:(farm uncle berry) AND v.name:\"Uncle Berry has a farm\"").vertices()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(VINDEX,"v.text:(beautiful are ducks)").vertices()));
        assertEquals(numV/strs.length*2-10,Iterables.size(graph.indexQuery(VINDEX,"v.text:(beautiful are ducks)").offset(10).vertices()));
        assertEquals(10,Iterables.size(graph.indexQuery(VINDEX,"v.\"text\":(beautiful are ducks)").limit(10).vertices()));
        assertEquals(10,Iterables.size(graph.indexQuery(VINDEX,"v.\"text\":(beautiful are ducks)").limit(10).offset(10).vertices()));
        assertEquals(0,Iterables.size(graph.indexQuery(VINDEX,"v.\"text\":(beautiful are ducks)").limit(10).offset(numV).vertices()));
        //Test name mapping
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(VINDEX,"vtext:ducks").vertices()));
        assertEquals(0,Iterables.size(graph.indexQuery(VINDEX,"etext:ducks").vertices()));


        //Same queries for edges
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(EINDEX,"e.text:ducks").edges()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(EINDEX,"e.text:(farm uncle berry)").edges()));
        assertEquals(numV/strs.length,Iterables.size(graph.indexQuery(EINDEX,"e.text:(farm uncle berry) AND e.name:\"Uncle Berry has a farm\"").edges()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(EINDEX,"e.text:(beautiful are ducks)").edges()));
        assertEquals(numV/strs.length*2-10,Iterables.size(graph.indexQuery(EINDEX,"e.text:(beautiful are ducks)").offset(10).edges()));
        assertEquals(10,Iterables.size(graph.indexQuery(EINDEX,"e.\"text\":(beautiful are ducks)").limit(10).edges()));
        assertEquals(10,Iterables.size(graph.indexQuery(EINDEX,"e.\"text\":(beautiful are ducks)").limit(10).offset(10).edges()));
        assertEquals(0,Iterables.size(graph.indexQuery(EINDEX,"e.\"text\":(beautiful are ducks)").limit(10).offset(numV).edges()));
        //Test name mapping
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(EINDEX,"etext:ducks").edges()));

        //Same queries for edges
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(PINDEX,"p.text:ducks").properties()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(PINDEX,"p.text:(farm uncle berry)").properties()));
        assertEquals(numV/strs.length,Iterables.size(graph.indexQuery(PINDEX,"p.text:(farm uncle berry) AND p.name:\"Uncle Berry has a farm\"").properties()));
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(PINDEX,"p.text:(beautiful are ducks)").properties()));
        assertEquals(numV/strs.length*2-10,Iterables.size(graph.indexQuery(PINDEX,"p.text:(beautiful are ducks)").offset(10).properties()));
        assertEquals(10,Iterables.size(graph.indexQuery(PINDEX,"p.\"text\":(beautiful are ducks)").limit(10).properties()));
        assertEquals(10,Iterables.size(graph.indexQuery(PINDEX,"p.\"text\":(beautiful are ducks)").limit(10).offset(10).properties()));
        assertEquals(0,Iterables.size(graph.indexQuery(PINDEX,"p.\"text\":(beautiful are ducks)").limit(10).offset(numV).properties()));
        //Test name mapping
        assertEquals(numV/strs.length*2,Iterables.size(graph.indexQuery(PINDEX,"ptext:ducks").properties()));
    }

   /* ==================================================================================
                                     TIME-TO-LIVE
     ==================================================================================*/

    @Test
    public void testVertexTTLWithMixedIndices() throws Exception {
        if (!features.hasCellTTL()) {
            return;
        }

        PropertyKey name = makeKey("name", String.class);
        PropertyKey time = makeKey("time", Long.class);
        PropertyKey text = makeKey("text", String.class);

        VertexLabel event = mgmt.makeVertexLabel("event").setStatic().make();
        mgmt.setTTL(event, 2, TimeUnit.SECONDS);

        mgmt.buildIndex("index1",Vertex.class).
                indexKey(name,Mapping.STRING.getParameter()).indexKey(time).buildMixedIndex(INDEX);
        mgmt.buildIndex("index2",Vertex.class).indexOnly(event).
                indexKey(text,Mapping.TEXT.getParameter()).buildMixedIndex(INDEX);

        assertEquals(0, mgmt.getTTL(name).getLength(TimeUnit.SECONDS));
        assertEquals(0, mgmt.getTTL(time).getLength(TimeUnit.SECONDS));
        assertEquals(2, mgmt.getTTL(event).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        Vertex v1 = tx.addVertex("event");
        v1.setProperty("name", "first event");
        v1.setProperty("text", "this text will help to identify the first event");
        long time1 = System.currentTimeMillis();
        v1.setProperty("time", time1);
        Vertex v2 = tx.addVertex("event");
        v2.setProperty("name", "second event");
        v2.setProperty("text", "this text won't match");
        long time2 = time1 + 1;
        v2.setProperty("time", time2);
        tx.commit();
        Object v1Id = v1.getId();
        Object v2Id = v2.getId();


        evaluateQuery(graph.query().has("text",Text.CONTAINS,"help").has("label","event"),
                ElementCategory.VERTEX,1,new boolean[]{true,true},"index2");
        evaluateQuery(graph.query().has("name","first event").orderBy(time,Order.DESC),
                ElementCategory.VERTEX,1,new boolean[]{true,true}, time, Order.DESC,"index1");
        v1 = graph.getVertex(v1Id);
        v2 = graph.getVertex(v1Id);
        assertNotNull(v1);
        assertNotNull(v2);


        Thread.sleep(2001);
        graph.rollback();

        evaluateQuery(graph.query().has("text",Text.CONTAINS,"help").has("label","event"),
                ElementCategory.VERTEX,1,new boolean[]{true,true},"index2");
        evaluateQuery(graph.query().has("name","first event").orderBy(time,Order.DESC),
                ElementCategory.VERTEX,1,new boolean[]{true,true}, time, Order.DESC,"index1");


        v1 = graph.getVertex(v1Id);
        v2 = graph.getVertex(v2Id);
        assertNull(v1);
        assertNull(v2);
    }

    @Test
    public void testEdgeTTLWithMixedIndices() throws Exception {
        if (!features.hasCellTTL()) {
            return;
        }

        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).make();
        PropertyKey text = mgmt.makePropertyKey("text").dataType(String.class).make();
        PropertyKey time = makeKey("time", Long.class);

        EdgeLabel label = mgmt.makeEdgeLabel("likes").make();
        mgmt.setTTL(label, 2, TimeUnit.SECONDS);

        mgmt.buildIndex("index1",Edge.class).
                indexKey(name,Mapping.STRING.getParameter()).indexKey(time).buildMixedIndex(INDEX);
        mgmt.buildIndex("index2",Edge.class).indexOnly(label).
                indexKey(text,Mapping.TEXT.getParameter()).buildMixedIndex(INDEX);

        assertEquals(0, mgmt.getTTL(name).getLength(TimeUnit.SECONDS));
        assertEquals(2, mgmt.getTTL(label).getLength(TimeUnit.SECONDS));
        mgmt.commit();

        Vertex v1 = graph.addVertex(null), v2 = graph.addVertex(null), v3 = graph.addVertex(null);

        Edge e1 = graph.addEdge(null, v1, v2, "likes");
        e1.setProperty("name", "v1 likes v2");
        e1.setProperty("text", "this will help to identify the edge");
        long time1 = System.currentTimeMillis();
        e1.setProperty("time", time1);
        Edge e2 = graph.addEdge(null, v2, v3, "likes");
        e2.setProperty("name", "v2 likes v3");
        e2.setProperty("text", "this won't match anything");
        long time2 = time1 + 1;
        e2.setProperty("time", time2);
        Object e1Id = e1.getId();
        Object e2Id = e2.getId();

        graph.commit();


        evaluateQuery(graph.query().has("text",Text.CONTAINS,"help").has("label","likes"),
                ElementCategory.EDGE,1,new boolean[]{true,true},"index2");
        evaluateQuery(graph.query().has("name","v2 likes v3").orderBy(time,Order.DESC),
                ElementCategory.EDGE,1,new boolean[]{true,true}, time, Order.DESC,"index1");
        v1 = graph.getVertex(v1);
        v2 = graph.getVertex(v2);
        v3 = graph.getVertex(v2);
        e1 = graph.getEdge(e1Id);
        e2 = graph.getEdge(e1Id);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v3);
        assertNotNull(e1);
        assertNotNull(e2);
        assertTrue(v1.getEdges(Direction.OUT).iterator().hasNext());
        assertTrue(v2.getEdges(Direction.OUT).iterator().hasNext());


        Thread.sleep(2001);

        graph.rollback();

        // ...indexes have expired
        evaluateQuery(graph.query().has("text",Text.CONTAINS,"help").has("label","likes"),
                ElementCategory.EDGE,0,new boolean[]{true,true},"index2");
        evaluateQuery(graph.query().has("name","v2 likes v3").orderBy(time,Order.DESC),
                ElementCategory.EDGE,0,new boolean[]{true,true}, time, Order.DESC,"index1");

        v1 = graph.getVertex(v1);
        v2 = graph.getVertex(v2);
        v3 = graph.getVertex(v2);
        e1 = graph.getEdge(e1Id);
        e2 = graph.getEdge(e1Id);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v3);
        // edges have expired from the graph...
        assertNull(e1);
        assertNull(e2);
        assertFalse(v1.getEdges(Direction.OUT).iterator().hasNext());
        assertFalse(v2.getEdges(Direction.OUT).iterator().hasNext());

    }

   /* ==================================================================================
                            SPECIAL CONCURRENT UPDATE CASES
     ==================================================================================*/

    /**
     * Create a vertex with an indexed property and commit. Open two new
     * transactions; delete vertex in one and delete just the property in the
     * other, then commit in the same order. Neither commit throws an exception.
     */
    @Test
    public void testDeleteVertexThenDeleteProperty() throws BackendException {
        testNestedWrites("x", null);
    }

    /**
     * Create a vertex and commit. Open two new transactions; delete vertex in
     * one and add an indexed property in the other, then commit in the same
     * order. Neither commit throws an exception.
     */
    @Test
    public void testDeleteVertexThenAddProperty() throws BackendException {
        testNestedWrites(null, "y");
    }

    /**
     * Create a vertex with an indexed property and commit. Open two new
     * transactions; delete vertex in one and modify the property in the other,
     * then commit in the same order. Neither commit throws an exception.
     */
    @Test
    public void testDeleteVertexThenModifyProperty() throws BackendException {
        testNestedWrites("x", "y");
    }

    private void testNestedWrites(String initialValue, String updatedValue) throws BackendException {
        // This method touches a single vertex with multiple transactions,
        // leading to deadlock under BDB and cascading test failures. Check for
        // the hasTxIsolation() store feature, which is currently true for BDB
        // but false for HBase/Cassandra. This is kind of a hack; a more robust
        // approach might implement different methods/assertions depending on
        // whether the store is capable of deadlocking or detecting conflicting
        // writes and aborting a transaction.
        Backend b = null;
        try {
            b = graph.getConfiguration().getBackend();
            if (b.getStoreFeatures().hasTxIsolation()) {
                log.info("Skipping "  + getClass().getSimpleName() + "." + methodName.getMethodName());
                return;
            }
        } finally {
            if (null != b)
                b.close();
        }

        final String propName = "foo";

        // Write schema and one vertex
        PropertyKey prop = makeKey(propName, String.class);
        createExternalVertexIndex(prop, INDEX);
        finishSchema();
        TitanVertex v = graph.addVertex(null);
        if (null != initialValue)
            ElementHelper.setProperties(v, propName, initialValue);
        graph.commit();

        Object id = v.getId();

        // Open two transactions and modify the same vertex
        TitanTransaction vertexDeleter = graph.newTransaction();
        TitanTransaction propDeleter = graph.newTransaction();

        vertexDeleter.removeVertex(vertexDeleter.getVertex(id));
        if (null == updatedValue)
            propDeleter.getVertex(propDeleter.getVertex(id)).removeProperty(propName);
        else
            propDeleter.getVertex(propDeleter.getVertex(id)).setProperty(propName, updatedValue);

        vertexDeleter.commit();
        propDeleter.commit();

        // The vertex must not exist after deletion
        graph.rollback();
        assertEquals(null,  graph.getVertex(id));
        assertEquals(false, graph.query().has(propName).vertices().iterator().hasNext());
        if (null != updatedValue)
            assertEquals(false, graph.query().has(propName, updatedValue).vertices().iterator().hasNext());
        graph.rollback();
    }
}