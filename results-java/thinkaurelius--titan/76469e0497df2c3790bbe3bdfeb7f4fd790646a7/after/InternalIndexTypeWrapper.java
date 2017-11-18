package com.thinkaurelius.titan.graphdb.types.indextype;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.types.*;
import com.tinkerpop.blueprints.Direction;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class InternalIndexTypeWrapper extends IndexTypeWrapper implements InternalIndexType {

    public InternalIndexTypeWrapper(SchemaSource base) {
        super(base);
    }

    @Override
    public boolean isInternalIndex() {
        return true;
    }

    @Override
    public boolean isExternalIndex() {
        return false;
    }

    @Override
    public long getID() {
        return base.getID();
    }

    @Override
    public SchemaStatus getStatus() {
        return base.getStatus();
    }

    IndexField[] fields = null;

    @Override
    public IndexField[] getFieldKeys() {
        if (fields==null) {
            Iterable<SchemaSource.Entry> entries = base.getRelated(TypeDefinitionCategory.INDEX_FIELD,Direction.OUT);
            int numFields = Iterables.size(entries);
            IndexField[] f = new IndexField[numFields];
            for (SchemaSource.Entry entry : entries) {
                Integer value = ParameterType.INDEX_POSITION.findParameter((Parameter[]) entry.getModifier(),null);
                Preconditions.checkNotNull(value);
                int pos = value;
                Preconditions.checkArgument(pos>=0 && pos<numFields,"Invalid field position: %s",pos);
                assert entry.getSchemaType() instanceof TitanKey;
                f[pos]=IndexField.of((TitanKey)entry.getSchemaType());
            }
            fields=f;
        }
        assert fields!=null;
        return fields;
    }

    @Override
    public Cardinality getCardinality() {
        return base.getDefinition().getValue(TypeDefinitionCategory.INDEX_CARDINALITY,Cardinality.class);
    }

    private ConsistencyModifier consistency = null;

    public ConsistencyModifier getConsistencyModifier() {
        if (consistency==null) {
            consistency = TypeUtil.getConsistencyModifier(base);
        }
        return consistency;
    }
}