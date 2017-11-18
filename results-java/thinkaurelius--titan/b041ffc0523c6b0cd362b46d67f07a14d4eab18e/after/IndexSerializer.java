package com.thinkaurelius.titan.graphdb.database;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.diskstorage.*;
import com.thinkaurelius.titan.diskstorage.indexing.*;
import com.thinkaurelius.titan.diskstorage.Entry;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeySliceQuery;
import com.thinkaurelius.titan.diskstorage.util.BufferUtil;
import com.thinkaurelius.titan.diskstorage.util.StaticArrayEntry;
import com.thinkaurelius.titan.graphdb.database.idhandling.VariableLong;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;
import com.thinkaurelius.titan.graphdb.database.serialize.AttributeUtil;
import com.thinkaurelius.titan.graphdb.database.serialize.DataOutput;
import com.thinkaurelius.titan.graphdb.database.serialize.Serializer;
import com.thinkaurelius.titan.graphdb.internal.*;
import com.thinkaurelius.titan.graphdb.query.graph.IndexQueryBuilder;
import com.thinkaurelius.titan.graphdb.query.TitanPredicate;
import com.thinkaurelius.titan.graphdb.query.condition.*;
import com.thinkaurelius.titan.graphdb.query.graph.JointIndexQuery;
import com.thinkaurelius.titan.graphdb.query.vertex.VertexCentricQueryBuilder;
import com.thinkaurelius.titan.graphdb.relations.RelationIdentifier;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.graphdb.types.*;
import com.thinkaurelius.titan.util.encoding.LongEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */

public class IndexSerializer {

    private static final Logger log = LoggerFactory.getLogger(IndexSerializer.class);

    private static final int DEFAULT_OBJECT_BYTELEN = 30;
    private static final byte FIRST_INDEX_COLUMN_BYTE = 0;

    private final Serializer serializer;
    private final Map<String, ? extends IndexInformation> externalIndexes;

    public IndexSerializer(Serializer serializer, Map<String, ? extends IndexInformation> indexes) {
        this.serializer = serializer;
        this.externalIndexes = indexes;
    }


    /* ################################################
               Index Information
    ################################################### */

    public boolean containsIndex(final String indexName) {
        return externalIndexes.containsKey(indexName);
    }

    public static void register(final ExternalIndexType index, final TitanKey key, final BackendTransaction tx) throws StorageException {
        tx.getIndexTransactionHandle(index.getBackingIndexName()).register(index.getStoreName(), key2Field(index,key), getKeyInformation(index.getField(key)));

    }

//    public boolean supports(final String indexName, final Class<?> dataType, final Parameter[] parameters) {
//        IndexInformation indexinfo = indexes.get(indexName);
//        Preconditions.checkArgument(indexinfo != null, "Index is unknown or not configured: %s", indexName);
//        return indexinfo.supports(new StandardKeyInformation(dataType,parameters));
//    }

    public boolean supports(final ExternalIndexType index, final ParameterIndexField field, final TitanPredicate predicate) {
        IndexInformation indexinfo = externalIndexes.get(index.getBackingIndexName());
        Preconditions.checkArgument(indexinfo != null, "Index is unknown or not configured: %s", index.getBackingIndexName());
        return indexinfo.supports(getKeyInformation(field),predicate);
    }

    private static StandardKeyInformation getKeyInformation(final ParameterIndexField field) {
        return new StandardKeyInformation(field.getFieldKey().getDataType(),field.getParameters());
    }

    public IndexInfoRetriever getIndexInfoRetriever() {
        return new IndexInfoRetriever();
    }

    public static class IndexInfoRetriever implements KeyInformation.Retriever {

        private StandardTitanTx transaction;

        private IndexInfoRetriever() {}

        public void setTransaction(StandardTitanTx tx) {
            Preconditions.checkNotNull(tx);
            Preconditions.checkArgument(transaction==null);
            transaction=tx;
        }

        @Override
        public KeyInformation.IndexRetriever get(final String index) {
            return new KeyInformation.IndexRetriever() {

                Map<String,KeyInformation.StoreRetriever> indexes = new ConcurrentHashMap<String, KeyInformation.StoreRetriever>();

                @Override
                public KeyInformation get(String store, String key) {
                    return get(store).get(key);
                }

                @Override
                public KeyInformation.StoreRetriever get(final String store) {
                    if (indexes.get(store)==null) {
                        Preconditions.checkState(transaction!=null,"Retriever has not been initialized");
                        final ExternalIndexType extIndex = getExternalIndex(store,transaction);
                        assert extIndex.getBackingIndexName().equals(index);
                        ImmutableMap.Builder<String,KeyInformation> b = ImmutableMap.builder();
                        for (ParameterIndexField field : extIndex.getFieldKeys()) b.put(key2Field(field),getKeyInformation(field));
                        final ImmutableMap<String,KeyInformation> infoMap = b.build();
                        KeyInformation.StoreRetriever storeRetriever = new KeyInformation.StoreRetriever() {

                            @Override
                            public KeyInformation get(String key) {
                                return infoMap.get(key);
                            }
                        };
                        indexes.put(store,storeRetriever);
                    }
                    return indexes.get(store);
                }

            };
        }
    }

    /* ################################################
               Index Updates
    ################################################### */

    public static class IndexUpdate<K,E> {

        private enum Type { ADD, DELETE };

        private final IndexType index;
        private final Type mutationType;
        private final K key;
        private final E entry;
        private final TitanElement element;

        private IndexUpdate(IndexType index, Type mutationType, K key, E entry, TitanElement element) {
            assert index!=null && mutationType!=null && key!=null && entry!=null && element!=null;
            assert !index.isInternalIndex() || (key instanceof StaticBuffer && entry instanceof Entry);
            assert !index.isExternalIndex() || (key instanceof String && entry instanceof IndexEntry);
            this.index = index;
            this.mutationType = mutationType;
            this.key = key;
            this.entry = entry;
            this.element = element;
        }

        public TitanElement getElement() {
            return element;
        }

        public IndexType getIndex() {
            return index;
        }

        public Type getType() {
            return mutationType;
        }

        public K getKey() {
            return key;
        }

        public E getEntry() {
            return entry;
        }

        public boolean isAddition() {
            return mutationType==Type.ADD;
        }

        public boolean isDeletion() {
            return mutationType==Type.DELETE;
        }

        public boolean isInternalIndex() {
            return index.isInternalIndex();
        }

        public boolean isExternalIndex() {
            return index.isExternalIndex();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(index).append(mutationType).append(key).append(entry).toHashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this==other) return true;
            else if (other==null || !(other instanceof IndexUpdate)) return false;
            IndexUpdate oth = (IndexUpdate)other;
            return index.equals(oth.index) && mutationType==oth.mutationType && key.equals(oth.key) && entry.equals(oth.entry);
        }
    }

    private static final IndexUpdate.Type getUpateType(InternalRelation relation) {
        assert relation.isNew() || relation.isRemoved();
        return (relation.isNew()? IndexUpdate.Type.ADD : IndexUpdate.Type.DELETE);
    }

    public Collection<IndexUpdate> getIndexUpdates(InternalRelation relation) {
        assert relation.isNew() || relation.isRemoved();
        Set<IndexUpdate> updates = Sets.newHashSet();
        IndexUpdate.Type updateType = getUpateType(relation);
        for (TitanType type : relation.getPropertyKeysDirect()) {
            if (!(type instanceof TitanKey)) continue;
            TitanKey key = (TitanKey)type;
            for (IndexType index : ((InternalType)key).getKeyIndexes()) {
                if (index instanceof InternalIndexType) {
                    InternalIndexType iIndex= (InternalIndexType) index;
                    if (iIndex.getStatus()== SchemaStatus.DISABLED) continue;
                    RecordEntry[] record = indexMatch(relation, iIndex);
                    if (record==null) continue;
                    updates.add(new IndexUpdate<StaticBuffer,Entry>(iIndex,updateType,getIndexKey(iIndex,record),getIndexEntry(iIndex,record,relation), relation));
                } else {
                    assert relation.getProperty(key)!=null;
                    if (((ExternalIndexType)index).getField(key).getStatus()== SchemaStatus.DISABLED) continue;
                    updates.add(getExternalIndexUpdate(relation,key,relation.getProperty(key),(ExternalIndexType)index,updateType));
                }
            }
        }
        return updates;
    }

    public Collection<IndexUpdate> getIndexUpdates(InternalVertex vertex, Collection<InternalRelation> updatedRelations) {
        if (updatedRelations.isEmpty()) return Collections.EMPTY_LIST;
        Set<IndexUpdate> updates = Sets.newHashSet();

        for (InternalRelation rel : updatedRelations) {
            if (!rel.isProperty()) continue;
            TitanProperty p = (TitanProperty)rel;
            assert rel.isNew() || rel.isRemoved(); assert rel.getVertex(0).equals(vertex);
            IndexUpdate.Type updateType = getUpateType(rel);
            for (IndexType index : ((InternalType)p.getPropertyKey()).getKeyIndexes()) {
                if (index.isInternalIndex()) { //Gather internal indexes
                    InternalIndexType iIndex = (InternalIndexType)index;
                    if (iIndex.getStatus()== SchemaStatus.DISABLED) continue;
                    IndexRecords updateRecords = indexMatches(vertex,iIndex,updateType==IndexUpdate.Type.DELETE,p.getPropertyKey(),new RecordEntry(p.getID(),p.getValue()));
                    for (RecordEntry[] record : updateRecords) {
                        updates.add(new IndexUpdate<StaticBuffer,Entry>(iIndex,updateType,getIndexKey(iIndex,record),getIndexEntry(iIndex,record,vertex), vertex));
                    }
                } else { //Update external indexes
                    if (((ExternalIndexType)index).getField(p.getPropertyKey()).getStatus()== SchemaStatus.DISABLED) continue;
                    updates.add(getExternalIndexUpdate(vertex,p.getPropertyKey(),p.getValue(),(ExternalIndexType)index,updateType));
                }
            }
        }
        return updates;
    }

    private IndexUpdate<String,IndexEntry> getExternalIndexUpdate(TitanElement element, TitanKey key, Object value,
                                                       ExternalIndexType index, IndexUpdate.Type updateType)  {
        return new IndexUpdate<String,IndexEntry>(index,updateType,element2String(element),new IndexEntry(key2Field(index.getField(key)), value), element);
    }

    public static RecordEntry[] indexMatch(TitanRelation element, InternalIndexType index) {
        IndexField[] fields = index.getFieldKeys();
        RecordEntry[] match = new RecordEntry[fields.length];
        for (int i = 0; i <fields.length; i++) {
            IndexField f = fields[i];
            Object value = element.getProperty(f.getFieldKey());
            if (value==null) return null; //No match
            match[i] = new RecordEntry(element.getID(),value);
        }
        return match;
    }

    public static class IndexRecords extends ArrayList<RecordEntry[]> {

        public boolean add(RecordEntry[] record) {
            return super.add(Arrays.copyOf(record,record.length));
        }

        public Iterable<Object[]> getRecordValues() {
            return Iterables.transform(this, new Function<RecordEntry[], Object[]>() {
                @Nullable
                @Override
                public Object[] apply(@Nullable RecordEntry[] record) {
                    return getValues(record);
                }
            });
        }

        private static Object[] getValues(RecordEntry[] record) {
            Object[] values = new Object[record.length];
            for (int i = 0; i < values.length; i++) {
                values[i]=record[i].value;
            }
            return values;
        }

    }

    private static class RecordEntry {

        final long relationId;
        final Object value;

        private RecordEntry(long relationId, Object value) {
            this.relationId = relationId;
            this.value = value;
        }
    }

    public static IndexRecords indexMatches(TitanVertex vertex, InternalIndexType index,
                                            TitanKey replaceKey, Object replaceValue) {
        IndexRecords matches = new IndexRecords();
        IndexField[] fields = index.getFieldKeys();
        indexMatches(vertex,new RecordEntry[fields.length],matches,fields,0,false,
                                            replaceKey,new RecordEntry(0,replaceValue));
        return matches;
    }

    private static IndexRecords indexMatches(TitanVertex vertex, InternalIndexType index,
                                              boolean onlyLoaded, TitanKey replaceKey, RecordEntry replaceValue) {
        IndexRecords matches = new IndexRecords();
        IndexField[] fields = index.getFieldKeys();
        indexMatches(vertex,new RecordEntry[fields.length],matches,fields,0,onlyLoaded,replaceKey,replaceValue);
        return matches;
    }

    private static void indexMatches(TitanVertex vertex, RecordEntry[] current, IndexRecords matches,
                                     IndexField[] fields, int pos,
                                     boolean onlyLoaded, TitanKey replaceKey, RecordEntry replaceValue) {
        if (pos>= fields.length) {
            matches.add(current);
            return;
        }

        TitanKey key = fields[pos].getFieldKey();

        List<RecordEntry> values;
        if (key.equals(replaceKey)) {
            values = ImmutableList.of(replaceValue);
        } else {
            values = new ArrayList<RecordEntry>();
            VertexCentricQueryBuilder qb = ((VertexCentricQueryBuilder)vertex.query()).type(key);
            if (onlyLoaded) qb.queryOnlyLoaded();
            for (TitanProperty p : qb.properties()) {
                assert p.isNew() || p.isLoaded(); assert !onlyLoaded || p.isLoaded();
                values.add(new RecordEntry(p.getID(),p.getValue()));
            }
        }
        for (RecordEntry value : values) {
            current[pos]=value;
            indexMatches(vertex,current,matches,fields,pos+1,onlyLoaded,replaceKey,replaceValue);
        }
    }


    /* ################################################
                Querying
    ################################################### */

    public List<Object> query(final JointIndexQuery.Subquery query, final BackendTransaction tx) {
        IndexType index = query.getIndex();
        if (index.isInternalIndex()) {
            KeySliceQuery sq = query.getInternalQuery();
            EntryList r = tx.indexQuery(sq);
            List<Object> results = new ArrayList<Object>(r.size());
            for (java.util.Iterator<Entry> iterator = r.reuseIterator(); iterator.hasNext(); ) {
                Entry entry = iterator.next();
                ReadBuffer entryValue = entry.asReadBuffer();
                entryValue.movePositionTo(entry.getValuePosition());
                switch(index.getElement()) {
                    case VERTEX:
                        results.add(VariableLong.readPositive(entryValue));
                        break;
                    default:
                        results.add(bytebuffer2RelationId(entryValue));
                }
            }
            Preconditions.checkArgument(((InternalIndexType)index).getCardinality()!=Cardinality.SINGLE || results.size() <= 1);
            return results;
        } else {
            List<String> r = tx.indexQuery(((ExternalIndexType) index).getBackingIndexName(), query.getExternalQuery());
            List<Object> result = new ArrayList<Object>(r.size());
            for (String id : r) result.add(string2ElementId(id));
            return result;
        }
    }

    public KeySliceQuery getQuery(final InternalIndexType index, Object[] values) {
        return new KeySliceQuery(getIndexKey(index,values), BufferUtil.zeroBuffer(1), BufferUtil.oneBuffer(1));
    }

    public IndexQuery getQuery(final ExternalIndexType index, final Condition condition, final OrderList orders) {
        Condition newCondition = ConditionUtil.literalTransformation(condition,
                new Function<Condition<TitanElement>, Condition<TitanElement>>() {
                    @Nullable
                    @Override
                    public Condition<TitanElement> apply(@Nullable Condition<TitanElement> condition) {
                        Preconditions.checkArgument(condition instanceof PredicateCondition);
                        PredicateCondition pc = (PredicateCondition) condition;
                        TitanKey key = (TitanKey) pc.getKey();
                        return new PredicateCondition<String, TitanElement>(key2Field(index,key), pc.getPredicate(), pc.getValue());
                    }
                });
        ImmutableList<IndexQuery.OrderEntry> newOrders = IndexQuery.NO_ORDER;
        if (!orders.isEmpty()) {
            ImmutableList.Builder<IndexQuery.OrderEntry> lb = ImmutableList.builder();
            for (int i = 0; i < orders.size(); i++) {
                lb.add(new IndexQuery.OrderEntry(key2Field(index,orders.getKey(i)), orders.getOrder(i), orders.getKey(i).getDataType()));
            }
            newOrders = lb.build();
        }
        return new IndexQuery(index.getStoreName(), newCondition, newOrders);
    }
//
//
//
//    public IndexQuery getQuery(String index, final ElementCategory resultType, final Condition condition, final OrderList orders) {
//        if (isStandardIndex(index)) {
//            Preconditions.checkArgument(orders.isEmpty());
//            return new IndexQuery(getStoreName(resultType), condition, IndexQuery.NO_ORDER);
//        } else {
//            Condition newCondition = ConditionUtil.literalTransformation(condition,
//                    new Function<Condition<TitanElement>, Condition<TitanElement>>() {
//                        @Nullable
//                        @Override
//                        public Condition<TitanElement> apply(@Nullable Condition<TitanElement> condition) {
//                            Preconditions.checkArgument(condition instanceof PredicateCondition);
//                            PredicateCondition pc = (PredicateCondition) condition;
//                            TitanKey key = (TitanKey) pc.getKey();
//                            return new PredicateCondition<String, TitanElement>(key2Field(key), pc.getPredicate(), pc.getValue());
//                        }
//                    });
//            ImmutableList<IndexQuery.OrderEntry> newOrders = IndexQuery.NO_ORDER;
//            if (!orders.isEmpty()) {
//                ImmutableList.Builder<IndexQuery.OrderEntry> lb = ImmutableList.builder();
//                for (int i = 0; i < orders.size(); i++) {
//                    lb.add(new IndexQuery.OrderEntry(key2Field(orders.getKey(i)), orders.getOrder(i), orders.getKey(i).getDataType()));
//                }
//                newOrders = lb.build();
//            }
//            return new IndexQuery(getStoreName(resultType), newCondition, newOrders);
//        }
//    }

    public Iterable<RawQuery.Result> executeQuery(IndexQueryBuilder query, final ElementCategory resultType,
                                                  final BackendTransaction backendTx, final StandardTitanTx transaction) {
        ExternalIndexType index = getExternalIndex(query.getIndex(),transaction);
        Preconditions.checkArgument(index.getElement()==resultType,"Index is not configured for the desired result type: %s",resultType);

        StringBuffer qB = new StringBuffer(query.getQuery());
        final String prefix = query.getPrefix();
        Preconditions.checkNotNull(prefix);
        //Convert query string by replacing
        int replacements = 0;
        int pos = 0;
        while (pos<qB.length()) {
            pos = qB.indexOf(prefix,pos);
            if (pos<0) break;

            int startPos = pos;
            pos += prefix.length();
            StringBuilder keyBuilder = new StringBuilder();
            boolean quoteTerminated = qB.charAt(pos)=='"';
            if (quoteTerminated) pos++;
            while (pos<qB.length() &&
                    (Character.isLetterOrDigit(qB.charAt(pos))
                            || (quoteTerminated && qB.charAt(pos)!='"')) ) {
                keyBuilder.append(qB.charAt(pos));
                pos++;
            }
            if (quoteTerminated) pos++;
            int endPos = pos;
            String keyname = keyBuilder.toString();
            Preconditions.checkArgument(StringUtils.isNotBlank(keyname),
                    "Found reference to empty key at position [%s]",startPos);
            String replacement;
            if (transaction.containsType(keyname)) {
                TitanKey key = transaction.getPropertyKey(keyname);
                Preconditions.checkNotNull(key);
                Preconditions.checkArgument(index.indexesKey(key),
                        "The used key [%s] is not indexed in the targeted index [%s]",key.getName(),query.getIndex());
                replacement = key2Field(index,key);
            } else {
                Preconditions.checkArgument(query.getUnknownKeyName()!=null,
                        "Found reference to non-existant property key in query at position [%s]: %s",startPos,keyname);
                replacement = query.getUnknownKeyName();
            }
            Preconditions.checkArgument(StringUtils.isNotBlank(replacement));

            qB.replace(startPos,endPos,replacement);
            pos = startPos+replacement.length();
            replacements++;
        }

        String queryStr = qB.toString();
        Preconditions.checkArgument(replacements>0,"Could not convert given %s index query: %s",resultType, query.getQuery());
        log.info("Converted query string with {} replacements: [{}] => [{}]",replacements,query.getQuery(),queryStr);
        RawQuery rawQuery=new RawQuery(index.getStoreName(),queryStr,query.getParameters());
        if (query.hasLimit()) rawQuery.setLimit(query.getLimit());
        return Iterables.transform(backendTx.rawQuery(query.getIndex(), rawQuery), new Function<RawQuery.Result<String>, RawQuery.Result>() {
            @Nullable
            @Override
            public RawQuery.Result apply(@Nullable RawQuery.Result<String> result) {
                return new RawQuery.Result(string2ElementId(result.getResult()), result.getScore());
            }
        });
    }


    /* ################################################
                Utility Functions
    ################################################### */

    private static final ExternalIndexType getExternalIndex(String indexName,StandardTitanTx transaction) {
        IndexType index = ManagementSystem.getGraphIndexDirect(indexName, transaction);
        Preconditions.checkArgument(index.isExternalIndex());
        return (ExternalIndexType)index;
    }

    private static final String element2String(TitanElement element) {
        if (element instanceof TitanVertex) return longID2Name(element.getID());
        else {
            RelationIdentifier rid = (RelationIdentifier) element.getId();
            return rid.toString();
        }
    }

    private static final Object string2ElementId(String str) {
        if (str.contains(RelationIdentifier.TOSTRING_DELIMITER)) return RelationIdentifier.parse(str);
        else return name2LongID(str);
    }

    private static final String key2Field(ExternalIndexType index, TitanKey key) {
        return key2Field(index.getField(key));
    }

    private static final String key2Field(ParameterIndexField field) {
        assert field!=null;
        return ParameterType.MAPPED_NAME.findParameter(field.getParameters(),longID2Name(field.getFieldKey().getID()));
    }

    private static final String longID2Name(long id) {
        Preconditions.checkArgument(id > 0);
        return LongEncoding.encode(id);
    }

    private static final long name2LongID(String name) {
        return LongEncoding.decode(name);
    }


    private final StaticBuffer getIndexKey(InternalIndexType index, RecordEntry[] record) {
        return getIndexKey(index,IndexRecords.getValues(record));
    }

    private final StaticBuffer getIndexKey(InternalIndexType index, Object[] values) {
        DataOutput out = serializer.getDataOutput(8*DEFAULT_OBJECT_BYTELEN + 8);
        IndexField[] fields = index.getFieldKeys();
        Preconditions.checkArgument(fields.length>0 && fields.length==values.length);
        for (int i = 0; i < fields.length; i++) {
            IndexField f = fields[i];
            Object value = values[i];
            Preconditions.checkNotNull(value);
            if (AttributeUtil.hasGenericDataType(f.getFieldKey())) out.writeClassAndObject(value);
            else out.writeObjectNotNull(value);
        }
        VariableLong.writePositiveBackward(out, index.getID());
        return out.getStaticBuffer();
    }

    private final Entry getIndexEntry(InternalIndexType index, RecordEntry[] record, TitanElement element) {
        DataOutput out = serializer.getDataOutput(1+8+8*record.length+4*8);
        out.putByte(FIRST_INDEX_COLUMN_BYTE);
        if (index.getCardinality()!=Cardinality.SINGLE) {
            VariableLong.writePositive(out,element.getID());
            if (index.getCardinality()!=Cardinality.SET) {
                for (RecordEntry re : record) {
                    VariableLong.writePositive(out,re.relationId);
                }
            }
        }
        int valuePosition=out.getPosition();
        if (element instanceof TitanVertex) {
            VariableLong.writePositive(out,element.getID());
        } else {
            assert element instanceof TitanRelation;
            RelationIdentifier rid = (RelationIdentifier)element.getId();
            long[] longs = rid.getLongRepresentation();
            Preconditions.checkArgument(longs.length == 3 || longs.length == 4);
            for (int i = 0; i < longs.length; i++) VariableLong.writePositive(out, longs[i]);
        }
        return new StaticArrayEntry(out.getStaticBuffer(),valuePosition);
    }

    private static final RelationIdentifier bytebuffer2RelationId(ReadBuffer b) {
        long[] relationId = new long[4];
        for (int i = 0; i < 3; i++) relationId[i] = VariableLong.readPositive(b);
        if (b.hasRemaining()) relationId[3] = VariableLong.readPositive(b);
        else relationId = Arrays.copyOfRange(relationId,0,3);
        return RelationIdentifier.get(relationId);
    }


}