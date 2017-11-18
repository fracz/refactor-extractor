package com.thinkaurelius.titan.core.schema;

import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.VertexLabel;

/**
 * When a graph is configured to automatically create edge labels and property keys when they are first used,
 * a DefaultTypeMaker implementation is used to define them by invoking the {@link #makeEdgeLabel(EdgeLabelMaker)}
 * or {@link #makePropertyKey(PropertyKeyMaker)} methods respectively.
 * <br />
 * By providing a custom DefaultTypeMaker implementation, one can specify how these types should be defined by default.
 * A DefaultTypeMaker implementation is specified in the graph configuration using the full path which means the
 * implementation must be on the classpath. For more information, see the
 * <a href="https://github.com/thinkaurelius/titan/wiki/Graph-Configuration">Graph Configuration Wiki</a>
 *
 * @author Matthias Br&ouml;cheler (http://www.matthiasb.com)
 * @see RelationTypeMaker
 * @see <a href="https://github.com/thinkaurelius/titan/wiki/Graph-Configuration">Graph Configuration Wiki</a>
 */
public interface DefaultSchemaMaker {

    /**
     * Creates a new label type with default settings against the provided {@link EdgeLabelMaker}.
     *
     * @param factory LabelMaker through which the edge label is created
     * @return A new edge label for the given name
     * @throws IllegalArgumentException if the name is already in use or if other configured values are invalid.
     */
    public EdgeLabel makeEdgeLabel(EdgeLabelMaker factory);

    /**
     * Creates a new property key with default settings against the provided {@link PropertyKeyMaker}.
     *
     * @param factory TypeMaker through which the property key is created
     * @return A new property key for the given name
     * @throws IllegalArgumentException if the name is already in use or if other configured values are invalid.
     */
    public PropertyKey makePropertyKey(PropertyKeyMaker factory);

    /**
     * Creates a new
     * @param factory
     * @return
     */
    public VertexLabel makeVertexLabel(VertexLabelMaker factory);

    /**
     * Whether to ignore undefined types occurring in a query.
     * <p/>
     * If this method returns true, then undefined types referred to in a {@link com.thinkaurelius.titan.core.TitanVertexQuery} will be silently
     * ignored and an empty result set will be returned. If this method returns false, then usage of undefined types
     * in queries results in an {@link IllegalArgumentException}.
     */
    public boolean ignoreUndefinedQueryTypes();
}