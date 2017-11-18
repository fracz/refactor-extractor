package com.thinkaurelius.titan.hadoop;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanProperty;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.graphdb.database.serialize.AttributeUtil;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.hadoop.conf.Configuration;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class StandardFaunusProperty extends StandardFaunusRelation implements FaunusProperty {

    protected long vertexid;
    protected Object value;

    public StandardFaunusProperty(final Configuration configuration) {
        super(configuration, FaunusElement.NO_ID, FaunusPropertyKey.VALUE);
    }

    public StandardFaunusProperty(final Configuration configuration, final DataInput in) throws IOException {
        super(configuration, FaunusElement.NO_ID, FaunusPropertyKey.VALUE);
        this.readFields(in);
    }

    public StandardFaunusProperty(FaunusVertex vertex, FaunusPropertyKey type, Object value) {
        this(FaunusElement.NO_ID, vertex, type, value);
    }

    public StandardFaunusProperty(long id, FaunusVertex vertex, String type, Object value) {
        this(id, vertex, vertex.getTypeManager().getPropertyKey(type), value);
    }

    public StandardFaunusProperty(long id, FaunusVertex vertex, FaunusPropertyKey type, Object value) {
        this(vertex.getConf(),id,vertex.getLongId(),type,value);
    }

    public StandardFaunusProperty(Configuration config, long id, long vertex, FaunusPropertyKey type, Object value) {
        super(config, id, type);
        Preconditions.checkArgument(!type.isImplicit(),"Cannot set implicit properties: " + type);
        setConf(config);
        Preconditions.checkArgument(vertex>=0);
        Preconditions.checkArgument(value!=null);
        Preconditions.checkArgument(AttributeUtil.hasGenericDataType(type) ||
                type.getDataType().isInstance(value),"Value does not match data type: %s",value);
        this.value = value;
        this.vertexid = vertex;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public PropertyKey getPropertyKey() {
        return (PropertyKey)type;
    }

    @Override
    public TitanVertex getVertex() {
        return getVertex(0);
    }

    @Override
    public TitanVertex getVertex(int pos) {
        Preconditions.checkArgument(pos==0,"Invalid position: %s",pos);
        return new FaunusVertex(getConf(), vertexid);
    }

    final void setKey(FaunusPropertyKey key) {
        Preconditions.checkNotNull(key);
        type = key;
    }


    //##################################
    // Serialization Proxy
    //##################################

    @Override
    public void write(final DataOutput out) throws IOException {
        new FaunusSerializer(this.getConf()).writeProperty(this, out);
    }

    @Override
    public void readFields(final DataInput in) throws IOException {
        new FaunusSerializer(this.getConf()).readProperty(this, in);

    }

    //##################################
    // General Utility
    //##################################


    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vertexid).append(getLongId()).append(type).append(value).toHashCode();
    }

    @Override
    public boolean equals(Object oth) {
        if (this == oth) return true;
        else if (oth == null || !(oth instanceof TitanProperty)) return false;
        TitanProperty p = (TitanProperty) oth;
        if (hasId() || p.hasId()) return getLongId()==p.getLongId();
        return type.equals(p.getPropertyKey()) && value.equals(p.getValue()) && vertexid==p.getVertex().getLongId();
    }

    @Override
    public String toString() {
        return getTypeName() + "->" + value.toString();
    }
}