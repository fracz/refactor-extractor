package com.thinkaurelius.titan.graphdb.types;

import com.google.common.base.Preconditions;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
* @author Matthias Broecheler (me@matthiasb.com)
*/
public class IndexType {

    private static final int VERTEX = 0;
    private static final int EDGE = 1;

    private static final int fromType(final Class<? extends Element> element) {
        if (element==Vertex.class) return VERTEX;
        else if (element==Edge.class) return EDGE;
        else throw new IllegalArgumentException("Either vertex or edge expected: " + element);
    }

    private static final Class<? extends Element> toType(final int element) {
        if (element==VERTEX) return Vertex.class;
        else if (element==EDGE) return Edge.class;
        else throw new IllegalArgumentException("Either vertex or edge expected: " + element);
    }

    private String indexName;
    private int element;

    public IndexType() {} //For serialization

    public IndexType(final String indexName, final Class<? extends Element> element) {
        this.indexName=indexName;
        this.element=fromType(element);
    }

    public String getIndexName() {
        return indexName;
    }

    public Class<? extends Element> getElementType() {
        return toType(element);
    }

    @Override
    public boolean equals(Object other) {
        if (this==other) return true;
        else if (!getClass().equals(other.getClass())) return false;
        IndexType oth = (IndexType)other;
        return element==oth.element && indexName.equals(oth.indexName);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(indexName).append(element).toHashCode();
    }

    @Override
    public String toString() {
        return indexName+':'+(element==VERTEX?"vertex":"edge");
    }

    public static IndexType valueOf(String definition) {
        int pos = definition.indexOf(':');
        Preconditions.checkArgument(pos>0,"Invalid IndexType definition: %s",definition);
        String indexName = definition.substring(0,pos-1);
        String element = definition.substring(pos+1);
        Class<? extends Element> elementClass;
        if (element.equalsIgnoreCase("vertex")) {
            elementClass = Vertex.class;
        } else if (element.equalsIgnoreCase("edge")) {
            elementClass = Edge.class;
        } else throw new IllegalArgumentException("Invalid IndexType definition: " + definition);
        return new IndexType(indexName,elementClass);
    }

}