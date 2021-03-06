package com.thinkaurelius.titan.hadoop.formats.graphson;

import com.thinkaurelius.titan.hadoop.StandardFaunusEdge;
import com.thinkaurelius.titan.hadoop.HadoopVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.graphson.ElementFactory;
import com.tinkerpop.blueprints.util.io.graphson.ElementPropertyConfig;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONTokens;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class HadoopGraphSONUtility {

    private static final String _OUT_E = "_outE";
    private static final String _IN_E = "_inE";
    private static final String EMPTY_STRING = "";

    private static final Set<String> VERTEX_IGNORE = new HashSet<String>(Arrays.asList(GraphSONTokens._TYPE, _OUT_E, _IN_E));
    private static final Set<String> EDGE_IGNORE = new HashSet<String>(Arrays.asList(GraphSONTokens._TYPE, GraphSONTokens._OUT_V, GraphSONTokens._IN_V));

    private static final HadoopElementFactory elementFactory = new HadoopElementFactory();

    private static final GraphSONUtility graphson = new GraphSONUtility(GraphSONMode.COMPACT, elementFactory,
            ElementPropertyConfig.excludeProperties(VERTEX_IGNORE, EDGE_IGNORE));

    public static List<HadoopVertex> fromJSON(final Configuration configuration, final InputStream in) throws IOException {
        final List<HadoopVertex> vertices = new LinkedList<HadoopVertex>();
        final BufferedReader bfs = new BufferedReader(new InputStreamReader(in));
        String line = "";
        while ((line = bfs.readLine()) != null) {
            vertices.add(HadoopGraphSONUtility.fromJSON(configuration, line));
        }
        bfs.close();
        return vertices;

    }

    public static HadoopVertex fromJSON(final Configuration configuration, String line) throws IOException {
        elementFactory.setConf(configuration);
        try {
            final JSONObject json = new JSONObject(new JSONTokener(line));
            line = EMPTY_STRING; // clear up some memory

            final HadoopVertex vertex = (HadoopVertex) graphson.vertexFromJson(json);
            vertex.setConf(configuration);

            fromJSONEdges(vertex, json.optJSONArray(_OUT_E), OUT);
            json.remove(_OUT_E); // clear up some memory
            fromJSONEdges(vertex, json.optJSONArray(_IN_E), IN);
            json.remove(_IN_E); // clear up some memory

            return vertex;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private static void fromJSONEdges(final HadoopVertex vertex, final JSONArray edges, final Direction direction) throws JSONException, IOException {
        if (null != edges) {
            for (int i = 0; i < edges.length(); i++) {
                final JSONObject edge = edges.optJSONObject(i);
                StandardFaunusEdge faunusEdge = null;
                if (direction.equals(Direction.IN)) {
                    faunusEdge = (StandardFaunusEdge) graphson.edgeFromJson(edge, new HadoopVertex(vertex.getConf(), edge.optLong(GraphSONTokens._OUT_V)), vertex);
                    faunusEdge.setConf(vertex.getConf());
                } else if (direction.equals(Direction.OUT)) {
                    faunusEdge = (StandardFaunusEdge) graphson.edgeFromJson(edge, vertex, new HadoopVertex(vertex.getConf(), edge.optLong(GraphSONTokens._IN_V)));
                    faunusEdge.setConf(vertex.getConf());
                }

                if (faunusEdge != null) {
                    vertex.addEdge(direction, faunusEdge);
                }
            }
        }
    }

    public static JSONObject toJSON(final Vertex vertex) throws IOException {
        try {
            final JSONObject object = GraphSONUtility.jsonFromElement(vertex, getElementPropertyKeys(vertex, false), GraphSONMode.COMPACT);

            // force the ID to long.  with blueprints, most implementations will send back a long, but
            // some like TinkerGraph will return a string.  the same is done for edges below
            object.put(GraphSONTokens._ID, Long.valueOf(object.remove(GraphSONTokens._ID).toString()));

            List<Edge> edges = (List<Edge>) vertex.getEdges(OUT);
            if (!edges.isEmpty()) {
                final JSONArray outEdgesArray = new JSONArray();
                for (final Edge outEdge : edges) {
                    final JSONObject edgeObject = GraphSONUtility.jsonFromElement(outEdge, getElementPropertyKeys(outEdge, true), GraphSONMode.COMPACT);
                    outEdgesArray.put(edgeObject);
                }
                object.put(_OUT_E, outEdgesArray);
            }

            edges = (List<Edge>) vertex.getEdges(IN);
            if (!edges.isEmpty()) {
                final JSONArray inEdgesArray = new JSONArray();
                for (final Edge inEdge : edges) {
                    final JSONObject edgeObject = GraphSONUtility.jsonFromElement(inEdge, getElementPropertyKeys(inEdge, false), GraphSONMode.COMPACT);
                    inEdgesArray.put(edgeObject);
                }
                object.put(_IN_E, inEdgesArray);
            }

            return object;
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static Set<String> getElementPropertyKeys(final Element element, final boolean edgeIn) {
        final Set<String> elementPropertyKeys = new HashSet<String>(element.getPropertyKeys());
        elementPropertyKeys.add(GraphSONTokens._ID);
        if (element instanceof Edge) {
            if (edgeIn) {
                elementPropertyKeys.add(GraphSONTokens._IN_V);
            } else {
                elementPropertyKeys.add(GraphSONTokens._OUT_V);
            }

            elementPropertyKeys.add(GraphSONTokens._LABEL);
        }

        return elementPropertyKeys;
    }

    private static class HadoopElementFactory implements ElementFactory<HadoopVertex, StandardFaunusEdge>, Configurable {

        private Configuration configuration;

        public void setConf(final Configuration configuration) {
            this.configuration = configuration;
        }

        public Configuration getConf() {
            return this.configuration;
        }

        @Override
        public StandardFaunusEdge createEdge(final Object id, final HadoopVertex out, final HadoopVertex in, final String label) {
            return new StandardFaunusEdge(this.configuration, convertIdentifier(id), out.getLongId(), in.getLongId(), label);
        }

        @Override
        public HadoopVertex createVertex(final Object id) {
            return new HadoopVertex(this.configuration, convertIdentifier(id));
        }

        private long convertIdentifier(final Object id) {
            if (id instanceof Long)
                return (Long) id;

            long identifier = -1l;
            if (id != null) {
                try {
                    identifier = Long.parseLong(id.toString());
                } catch (final NumberFormatException e) {
                    identifier = -1l;
                }
            }
            return identifier;
        }
    }
}