package com.thinkaurelius.titan.graphdb.types.system;

import com.google.common.collect.ImmutableMap;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.graphdb.internal.InternalVertexLabel;

import java.util.Map;

public abstract class SystemTypeManager {

    public static final String systemETprefix = "system%&%";

    private volatile static Map<Long, SystemRelationType> SYSTEM_TYPES_BY_ID;
    private volatile static Map<String, SystemRelationType> SYSTEM_TYPES_BY_NAME;

    static {
        synchronized (SystemTypeManager.class) {
            ImmutableMap.Builder<Long, SystemRelationType> idBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<String, SystemRelationType> nameBuilder = ImmutableMap.builder();
            for (SystemRelationType et : new SystemRelationType[]{BaseKey.SchemaCategory, BaseKey.SchemaDefinitionDesc,
                    BaseKey.SchemaDefinitionProperty, BaseKey.SchemaName,
                    BaseKey.VertexExists,
                    BaseLabel.VertexLabelEdge, BaseLabel.SchemaDefinitionEdge,
                    ImplicitKey.ID, ImplicitKey.LABEL, ImplicitKey.ADJACENT_ID,
                    ImplicitKey.TIMESTAMP, ImplicitKey.TTL, ImplicitKey.VISIBILITY
                }) {
                if (et.hasId()) idBuilder.put(et.getID(), et);
                nameBuilder.put(et.getName(),et);
            }

            SYSTEM_TYPES_BY_ID = idBuilder.build();
            SYSTEM_TYPES_BY_NAME = nameBuilder.build();
        }
        assert SYSTEM_TYPES_BY_ID.size()==11;
        assert SYSTEM_TYPES_BY_NAME.size()==13;
    }

    public static SystemRelationType getSystemType(long id) {
        return SYSTEM_TYPES_BY_ID.get(id);
    }

    public static SystemRelationType getSystemType(String name) {
        return SYSTEM_TYPES_BY_NAME.get(name);
    }

    public static boolean isSystemType(String name) {
        return SYSTEM_TYPES_BY_NAME.containsKey(name);
    }


}