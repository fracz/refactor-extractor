package com.thinkaurelius.titan.graphdb.util;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.attribute.Cmp;
import com.thinkaurelius.titan.graphdb.query.graph.GraphCentricQueryBuilder;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.graphdb.types.IndexField;
import com.thinkaurelius.titan.graphdb.types.InternalIndexType;
import com.tinkerpop.blueprints.Element;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class IndexHelper {
    public static Iterable<? extends Element> getQueryResults(InternalIndexType index, Object[] values, StandardTitanTx tx) {
        GraphCentricQueryBuilder gb = getQuery(index,values,tx);
        switch(index.getElement()) {
            case VERTEX:
                return gb.vertices();
            case EDGE:
                return gb.edges();
            case PROPERTY:
                return gb.properties();
            default: throw new AssertionError();
        }
    }

    public static GraphCentricQueryBuilder getQuery(InternalIndexType index, Object[] values, StandardTitanTx tx) {
        Preconditions.checkArgument(index != null && values != null && values.length > 0 && tx != null);
        Preconditions.checkArgument(values.length==index.getFieldKeys().length);
        GraphCentricQueryBuilder gb = tx.query();
        IndexField[] fields = index.getFieldKeys();
        for (int i = 0; i <fields.length; i++) {
            IndexField f = fields[i];
            Object value = values[i];
            Preconditions.checkNotNull(value);
            TitanKey key = f.getFieldKey();
            Preconditions.checkArgument(key.getDataType().equals(value.getClass()),"Incompatible data types");
            gb.has(key, Cmp.EQUAL,value);
        }
        return gb;
    }
}