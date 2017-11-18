package com.thinkaurelius.titan.graphdb.database.cache;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkaurelius.titan.diskstorage.EntryList;
import com.thinkaurelius.titan.graphdb.idmanagement.IDManager;
import com.thinkaurelius.titan.graphdb.relations.EdgeDirection;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.graphdb.types.system.SystemRelationType;
import com.thinkaurelius.titan.graphdb.types.system.SystemTypeManager;
import com.tinkerpop.blueprints.Direction;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class StandardSchemaCache implements SchemaCache {

    public static final int MAX_CACHED_TYPES_DEFAULT = 10000;

    private static final int INITIAL_CAPACITY = 128;
    private static final int INITIAL_CACHE_SIZE = 16;
    private static final int CACHE_RELATION_MULTIPLIER = 3; // 1) type-name, 2) type-definitions, 3) modifying edges [index, lock]
    private static final int CONCURRENCY_LEVEL = 2;

    private static final int SCHEMAID_FORW_SHIFT = 4; //Number of bits at the end to append the id of the system type
    private static final int SCHEMAID_TOTALFORW_SHIFT = SCHEMAID_FORW_SHIFT +1; //Total number of bits appended - the 1 is for the 1 bit direction
    private static final int SCHEMAID_BACK_SHIFT = 3; //Number of bits to remove from end of schema id since its just the padding
    {
        assert IDManager.VertexIDType.SchemaType.removePadding(1l<<SCHEMAID_BACK_SHIFT)==1;
        assert SystemTypeManager.SYSTEM_RELATIONTYPE_OFFSET <= (1<< SCHEMAID_FORW_SHIFT);
        assert SCHEMAID_TOTALFORW_SHIFT-SCHEMAID_BACK_SHIFT>=0;
    }

    private final int maxCachedTypes;
    private final int maxCachedRelations;
    private final StoreRetrieval retriever;

    private volatile ConcurrentMap<String,Long> typeNames;
    private final Cache<String,Long> typeNamesBackup;

    private volatile ConcurrentMap<Long,EntryList> schemaRelations;
    private final Cache<Long,EntryList> schemaRelationsBackup;

    public StandardSchemaCache(final StoreRetrieval retriever) {
        this(MAX_CACHED_TYPES_DEFAULT,retriever);
    }

    public StandardSchemaCache(final int size, final StoreRetrieval retriever) {
        Preconditions.checkArgument(size>0,"Size must be positive");
        Preconditions.checkNotNull(retriever);
        maxCachedTypes = size;
        maxCachedRelations = maxCachedTypes *CACHE_RELATION_MULTIPLIER;
        this.retriever=retriever;

        typeNamesBackup = CacheBuilder.newBuilder()
                .concurrencyLevel(CONCURRENCY_LEVEL).initialCapacity(INITIAL_CACHE_SIZE)
                .maximumSize(maxCachedTypes).build();
        typeNames = new ConcurrentHashMap<String, Long>(INITIAL_CAPACITY,0.75f,CONCURRENCY_LEVEL);

        schemaRelationsBackup = CacheBuilder.newBuilder()
                .concurrencyLevel(CONCURRENCY_LEVEL).initialCapacity(INITIAL_CACHE_SIZE *CACHE_RELATION_MULTIPLIER)
                .maximumSize(maxCachedRelations).build();
//        typeRelations = new ConcurrentHashMap<Long, EntryList>(INITIAL_CAPACITY*CACHE_RELATION_MULTIPLIER,0.75f,CONCURRENCY_LEVEL);
        schemaRelations = new NonBlockingHashMapLong<EntryList>(INITIAL_CAPACITY*CACHE_RELATION_MULTIPLIER); //TODO: Is this data structure safe or should we go with ConcurrentHashMap (line above)?
    }


    @Override
    public Long getTypeId(final String typeName, final StandardTitanTx tx) {
        ConcurrentMap<String,Long> types = typeNames;
        Long id;
        if (types==null) {
            id = typeNamesBackup.getIfPresent(typeName);
            if (id==null) {
                id = retriever.retrieveTypeByName(typeName, tx);
                if (id!=null) { //only cache if type exists
                    typeNamesBackup.put(typeName,id);
                }
            }
        } else {
            id = types.get(typeName);
            if (id==null) { //Retrieve it
                if (types.size()> maxCachedTypes) {
                    /* Safe guard against the concurrent hash map growing to large - this would be a VERY rare event
                    as it only happens for graph databases with thousands of types.
                     */
                    typeNames = null;
                    return getTypeId(typeName, tx);
                } else {
                    //Expand map
                    id = retriever.retrieveTypeByName(typeName, tx);
                    if (id!=null) { //only cache if type exists
                        types.put(typeName,id);
                    }
                }
            }
        }
        return id;
    }

    @Override
    public EntryList getTypeRelations(final long schemaId, final SystemRelationType type, final Direction dir, final StandardTitanTx tx) {
        assert IDManager.getRelationTypeIdCount(type.getID())<SystemTypeManager.SYSTEM_RELATIONTYPE_OFFSET;
        Preconditions.checkArgument(IDManager.VertexIDType.SchemaType.is(schemaId));
        Preconditions.checkArgument((Long.MAX_VALUE>>>(SCHEMAID_TOTALFORW_SHIFT-SCHEMAID_BACK_SHIFT))>= schemaId);

        int edgeDir = EdgeDirection.position(dir);
        assert edgeDir==0 || edgeDir==1;

        final long typePlusRelation = ((((schemaId >>> SCHEMAID_BACK_SHIFT) << 1) + edgeDir) << SCHEMAID_FORW_SHIFT) + IDManager.getRelationTypeIdCount(type.getID());
        ConcurrentMap<Long,EntryList> types = schemaRelations;
        EntryList entries;
        if (types==null) {
            entries = schemaRelationsBackup.getIfPresent(typePlusRelation);
            if (entries==null) {
                entries = retriever.retrieveTypeRelations(schemaId,type,dir,tx);
                if (!entries.isEmpty()) { //only cache if type exists
                    schemaRelationsBackup.put(typePlusRelation, entries);
                }
            }
        } else {
            entries = types.get(typePlusRelation);
            if (entries==null) { //Retrieve it
                if (types.size()> maxCachedRelations) {
                    /* Safe guard against the concurrent hash map growing to large - this would be a VERY rare event
                    as it only happens for graph databases with thousands of types.
                     */
                    schemaRelations = null;
                    return getTypeRelations(schemaId, type, dir, tx);
                } else {
                    //Expand map
                    entries = retriever.retrieveTypeRelations(schemaId, type, dir, tx);
                    if (!entries.isEmpty()) { //only cache if type exists
                        types.put(typePlusRelation,entries);
                    }
                }
            }
        }
        assert entries!=null;
        return entries;
    }

    @Override
    public void expireTypeName(final String name) {
        ConcurrentMap<String,Long> types = typeNames;
        if (types!=null) types.remove(name);
        typeNamesBackup.invalidate(name);
    }

    @Override
    public void expireTypeRelations(final long schemaId) {
        final long cuttypeid = (schemaId >>> SCHEMAID_BACK_SHIFT);
        ConcurrentMap<Long,EntryList> types = schemaRelations;
        if (types!=null) {
            Iterator<Long> keys = types.keySet().iterator();
            while (keys.hasNext()) {
                long key = keys.next();
                if ((key>>>SCHEMAID_TOTALFORW_SHIFT)==cuttypeid) keys.remove();
            }
        }
        Iterator<Long> keys = schemaRelationsBackup.asMap().keySet().iterator();
        while (keys.hasNext()) {
            long key = keys.next();
            if ((key>>>SCHEMAID_TOTALFORW_SHIFT)==cuttypeid) schemaRelationsBackup.invalidate(key);
        }
    }

}