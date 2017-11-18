package com.thinkaurelius.titan.graphdb.database;

import com.carrotsearch.hppc.LongOpenHashSet;
import com.carrotsearch.hppc.LongSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.attribute.Cmp;
import com.thinkaurelius.titan.diskstorage.ReadBuffer;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.Entry;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.SliceQuery;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StaticBufferEntry;
import com.thinkaurelius.titan.diskstorage.util.ByteBufferUtil;
import com.thinkaurelius.titan.graphdb.database.idhandling.IDHandler;
import com.thinkaurelius.titan.graphdb.database.idhandling.VariableLong;
import com.thinkaurelius.titan.graphdb.database.serialize.DataOutput;
import com.thinkaurelius.titan.graphdb.database.serialize.Serializer;
import com.thinkaurelius.titan.graphdb.idmanagement.IDManager;
import com.thinkaurelius.titan.graphdb.internal.InternalRelation;
import com.thinkaurelius.titan.graphdb.internal.InternalType;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.internal.RelationType;
import com.thinkaurelius.titan.graphdb.query.VertexCentricQuery;
import com.thinkaurelius.titan.graphdb.query.keycondition.KeyAtom;
import com.thinkaurelius.titan.graphdb.relations.CacheEdge;
import com.thinkaurelius.titan.graphdb.relations.CacheProperty;
import com.thinkaurelius.titan.graphdb.relations.EdgeDirection;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;
import com.thinkaurelius.titan.util.datastructures.ImmutableLongObjectMap;
import static com.thinkaurelius.titan.graphdb.idmanagement.IDManager.*;

import com.thinkaurelius.titan.util.datastructures.Interval;
import com.tinkerpop.blueprints.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */

public class EdgeSerializer {

    private static final Logger log = LoggerFactory.getLogger(EdgeSerializer.class);


    private static final int DEFAULT_PRIMARY_CAPACITY = 60;
    private static final int DEFAULT_VALUE_CAPACITY = 128;

    private static final long DIRECTION_ID = -101;
    private static final long TYPE_ID = -102;
    private static final long VALUE_ID = -103;
    private static final long OTHER_VERTEX_ID = -104;
    private static final long RELATION_ID = -105;

    private final Serializer serializer;
    private final IDManager idManager;

    public EdgeSerializer(Serializer serializer, IDManager idManager) {
        this.serializer = serializer;
        this.idManager = idManager;
    }

    public InternalRelation readRelation(InternalVertex vertex, Entry data) {
        StandardTitanTx tx = vertex.tx();
        ImmutableLongObjectMap map = getProperties(vertex.getID(),data,true,tx);

        Direction dir = (Direction) map.get(DIRECTION_ID);
        long typeid = (Long)map.get(TYPE_ID);
        TitanType type = tx.getExistingType(typeid);
        long relationId = (Long)map.get(RELATION_ID);
        if (type.isPropertyKey()) {
            Preconditions.checkArgument(dir==Direction.OUT);
            Object value = map.get(VALUE_ID);
            return new CacheProperty(relationId,(TitanKey)type,vertex,value,data);
        } else if (type.isEdgeLabel()) {
            long otherid = (Long)map.get(OTHER_VERTEX_ID);
            InternalVertex otherv = tx.getExistingVertex(otherid);
            if (dir==Direction.IN) {
                return new CacheEdge(relationId,(TitanLabel)type,otherv,vertex,(byte)1,data);
            } else if (dir==Direction.OUT) {
                return new CacheEdge(relationId,(TitanLabel)type,vertex,otherv,(byte)0,data);
            } else throw new AssertionError();
        } else throw new AssertionError();
    }

    public void readRelation(RelationFactory factory, Entry data, StandardTitanTx tx) {
        ImmutableLongObjectMap map = getProperties(factory.getVertexID(),data,false,tx);

        factory.setDirection((Direction) map.get(DIRECTION_ID));
        long typeid = (Long)map.get(TYPE_ID);
        TitanType type = tx.getExistingType(typeid);
        factory.setType(type);
        factory.setRelationID((Long) map.get(RELATION_ID));
        if (type.isPropertyKey()) {
            factory.setValue(map.get(VALUE_ID));
        } else if (type.isEdgeLabel()) {
            factory.setOtherVertexID((Long)map.get(OTHER_VERTEX_ID));
        } else throw new AssertionError();
        //Add properties
        for (int i=0;i<map.size();i++) {
            long propTypeId = map.getKey(i);
            if (propTypeId>0) {
                TitanType pt = tx.getExistingType(propTypeId);
                if (map.getValue(i)!=null) {
                    factory.addProperty(pt,map.getValue(i));
                }
            }
        }
    }

    public ImmutableLongObjectMap readProperties(InternalVertex vertex, Entry data, StandardTitanTx tx) {
        return getProperties(vertex.getID(),data, false, tx);
    }

    public ImmutableLongObjectMap getProperties(long vertexid, Entry data, boolean parseHeaderOnly, StandardTitanTx tx) {
        ImmutableLongObjectMap map = data.getCache();
        if (map==null) {
//                synchronized (data) {
//                    if (data.getCache()==null) {
                        map = parseProperties(vertexid,data,parseHeaderOnly,tx);
                        if (!parseHeaderOnly) data.setCache(map);
//                    } else map = data.getCache();
//                }
        }
        return map;
    }


    private ImmutableLongObjectMap parseProperties(long vertexid, Entry data, boolean parseHeaderOnly, StandardTitanTx tx) {
        Preconditions.checkArgument(vertexid>0);
        ImmutableLongObjectMap.Builder builder = new ImmutableLongObjectMap.Builder();

        ReadBuffer column = data.getReadColumn();
        ReadBuffer value = data.getReadValue();

        long[] typeAndDir = IDHandler.readEdgeType(column);
        int dirID = (int)typeAndDir[1];
        long typeId = typeAndDir[0];

        Direction dir=null;
        RelationType rtype=null;
        switch(dirID) {
            case PROPERTY_DIR: dir=Direction.OUT; rtype=RelationType.PROPERTY; break;
            case EDGE_OUT_DIR: dir=Direction.OUT; rtype=RelationType.EDGE; break;
            case EDGE_IN_DIR: dir=Direction.IN; rtype=RelationType.EDGE; break;
            default: throw new IllegalArgumentException("Invalid dirID read from disk: " + dirID);
        }
        builder.put(DIRECTION_ID,dir);
        builder.put(TYPE_ID,typeId);
        TitanType titanType = tx.getExistingType(typeId);

        InternalType def = (InternalType) titanType;
        if (!parseHeaderOnly) {
            long[] keysig = def.getPrimaryKey();
            if (keysig.length>0) {
                for (int i = 0; i < keysig.length; i++) {
                    TitanType keyType = tx.getExistingType(keysig[i]);
                    builder.put(keyType.getID(),readInline(column, keyType));
                }
            }
        }

        long relationIdDiff, vertexIdDiff=0;
        if (titanType.isUnique(dir)) {
            if (rtype==RelationType.EDGE) vertexIdDiff = VariableLong.read(value);
            relationIdDiff=VariableLong.read(value);
        } else {
            //Move position to end to read backwards
            column.movePosition(column.length()-column.getPosition()-1);

            relationIdDiff=VariableLong.readBackward(column);
            if (rtype==RelationType.EDGE) vertexIdDiff=VariableLong.readBackward(column);
        }
        Preconditions.checkArgument(relationIdDiff+vertexid>0);
        builder.put(RELATION_ID,relationIdDiff+vertexid);

        if (rtype==RelationType.EDGE) {
            Preconditions.checkArgument(titanType.isEdgeLabel());
            builder.put(OTHER_VERTEX_ID,vertexid+vertexIdDiff);
        }

        if (rtype==RelationType.PROPERTY) {
            Preconditions.checkArgument(titanType.isPropertyKey());
            TitanKey key = ((TitanKey) titanType);
            Object attribute = null;

            if (hasGenericDataType(key)) {
                attribute = serializer.readClassAndObject(value);
            } else {
                attribute = serializer.readObjectNotNull(value, key.getDataType());
            }
            Preconditions.checkNotNull(attribute);
            builder.put(VALUE_ID,attribute);
        }

        if (!parseHeaderOnly) {
            //value signature
            for (long typeID : def.getSignature())
                builder.put(typeID,readInline(value,tx.getExistingType(typeID)));

            //Third: read rest
            while (value.hasRemaining()) {
                TitanType type = tx.getExistingType(IDHandler.readInlineEdgeType(value));
                builder.put(type.getID(), readInline(value, type));
            }
        }

        return builder.build();
    }

    private Object readInline(ReadBuffer read, TitanType type) {
        if (type.isPropertyKey()) {
            TitanKey proptype = ((TitanKey) type);
            if (hasGenericDataType(proptype))
                return serializer.readClassAndObject(read);
            else return serializer.readObject(read, proptype.getDataType());
        } else {
            assert type.isEdgeLabel();
            Long id = Long.valueOf(VariableLong.readPositive(read));
            if (id.longValue() == 0) return null;
            else return id;
        }
    }

    private static final boolean hasGenericDataType(TitanKey key) {
        return key.getDataType().equals(Object.class);
    }


    public Entry writeRelation(InternalRelation relation, int pos, StandardTitanTx tx) {
        return writeRelation(relation,pos,true,tx);
    }

    public Entry writeRelation(InternalRelation relation, int position, boolean writeValue, StandardTitanTx tx) {
        Preconditions.checkArgument(position<relation.getLen());
        TitanType type = relation.getType();
        long typeid = type.getID();

        int dirID;
        Direction dir = EdgeDirection.fromPosition(position);
        if (relation.isProperty()) {
            dirID = PROPERTY_DIR;
        } else if (position==0) {
            //Out Relation
            Preconditions.checkArgument(relation.isEdge());
            dirID = EDGE_OUT_DIR;
        } else if (position==1) {
            //In Relation
            Preconditions.checkArgument(relation.isEdge());
            dirID = EDGE_IN_DIR;
        } else {
            throw new IllegalArgumentException("Invalid position: " + position);
        }

        //int typeIDLength = IDHandler.edgeTypeLength(typeid, idManager);

        DataOutput colOut = serializer.getDataOutput(DEFAULT_PRIMARY_CAPACITY, true);
        IDHandler.writeEdgeType(colOut, typeid, dirID);

        InternalType definition = (InternalType)type;
        long[] primaryKey = definition.getPrimaryKey();
        for (int i=0;i<primaryKey.length;i++) {
            TitanType t = tx.getExistingType(primaryKey[i]);
            writeInline(colOut, t, relation.getProperty(t), false);
        }


        DataOutput writer = colOut;
        long vertexIdDiff = 0;
        long relationIdDiff = relation.getID() - relation.getVertex(position).getID();
        if (relation.isEdge()) vertexIdDiff = relation.getVertex((position+1)%2).getID() - relation.getVertex(position).getID();

        if (type.isUnique(dir)) {
            if (!writeValue) return new StaticBufferEntry(colOut.getStaticBuffer(),null);
            writer = serializer.getDataOutput(DEFAULT_VALUE_CAPACITY, true);
            if (relation.isEdge()) VariableLong.write(writer,vertexIdDiff);
            VariableLong.write(writer,relationIdDiff);
        } else {
            if (relation.isEdge()) VariableLong.writeBackward(writer,vertexIdDiff);
            VariableLong.writeBackward(writer,relationIdDiff);
        }

        if (!type.isUnique(dir)) {
            if (!writeValue) return new StaticBufferEntry(colOut.getStaticBuffer(),null);
            writer = serializer.getDataOutput(DEFAULT_VALUE_CAPACITY, true);
        }

        if (relation.isProperty()) {
            Preconditions.checkArgument(relation.isProperty());
            Object value = ((TitanProperty)relation).getValue();
            Preconditions.checkNotNull(value);
            TitanKey key = (TitanKey)type;
            assert key.getDataType().isInstance(value);
            if (hasGenericDataType(key)) {
                writer.writeClassAndObject(value);
            } else {
                writer.writeObjectNotNull(value);
            }
        }

        //Write signature
        long[] signature = definition.getSignature();
        for (int i=0;i<signature.length;i++) {
            TitanType t = tx.getExistingType(signature[i]);
            writeInline(writer, t, relation.getProperty(t), false);
        }

        //Write remaining properties
        LongSet writtenTypes = new LongOpenHashSet(primaryKey.length + signature.length);
        if (primaryKey.length>0 || signature.length>0) {
            for (long id : primaryKey) writtenTypes.add(id);
            for (long id : signature) writtenTypes.add(id);
        }
        for (TitanType t : relation.getPropertyKeysDirect()) {
            if (!writtenTypes.contains(t.getID())) {
                writeInline(writer,t,relation.getProperty(t),true);
            }
        }

        return new StaticBufferEntry(colOut.getStaticBuffer(), writer.getStaticBuffer());
    }

    private void writeInline(DataOutput out, TitanType type, Object value, boolean writeEdgeType) {
        Preconditions.checkArgument(!(type.isPropertyKey() && !writeEdgeType) || !hasGenericDataType((TitanKey)type));

        if (writeEdgeType) {
            IDHandler.writeInlineEdgeType(out, type.getID());
        }

        if (type.isPropertyKey()) {
            if (hasGenericDataType((TitanKey) type)) {
                out.writeClassAndObject(value);
            } else {
                out.writeObject(value,((TitanKey) type).getDataType());
            }
        } else {
            assert type.isEdgeLabel();
            Preconditions.checkArgument(((TitanLabel)type).isUnidirected());
            if (value==null) {
                VariableLong.writePositive(out, 0);
            } else {
                VariableLong.writePositive(out, ((InternalVertex)value).getID());
            }
        }
    }


    private static int[] getDirIDInterval(Direction dir, RelationType rt) {
        if (dir==Direction.OUT) {
            if (rt== RelationType.PROPERTY) return new int[]{PROPERTY_DIR,PROPERTY_DIR};
            else if (rt== RelationType.EDGE) return new int[]{EDGE_OUT_DIR,EDGE_OUT_DIR};
            else if (rt== RelationType.RELATION) return new int[]{PROPERTY_DIR,EDGE_OUT_DIR};
            else throw new IllegalArgumentException("Invalid dir+return-type: " + dir + "|" + rt);
        } else if (dir==Direction.IN) {
            if (rt== RelationType.EDGE) return new int[]{EDGE_IN_DIR,EDGE_IN_DIR};
            else throw new IllegalArgumentException("Invalid dir+return-type: " + dir + "|" + rt);
        } else if (dir==Direction.BOTH) {
            if (rt== RelationType.PROPERTY) return new int[]{PROPERTY_DIR,PROPERTY_DIR};
            else if (rt== RelationType.EDGE) return new int[]{EDGE_OUT_DIR,EDGE_IN_DIR};
            else if (rt== RelationType.RELATION) return new int[]{PROPERTY_DIR,EDGE_IN_DIR};
            else throw new IllegalArgumentException("Invalid dir+return-type: " + dir + "|" + rt);
        } else throw new IllegalArgumentException("Invalid dir+return-type: " + dir + "|" + rt);
    }

    private static int getDirID(Direction dir, RelationType rt) {
        int[] ids = getDirIDInterval(dir,rt);
        Preconditions.checkArgument(ids[0]==ids[1],"Invalid arguments [%s] [%s]",dir,rt);
        return ids[0];
    }

//    public SliceQuery getQuery(RelationType resultType, Direction dir) {
//
//    }
//
//    public SliceQuery getQuery(InternalType type, Direction dir, List<Interval> primaryKey, VertexConstraint vertexCon) {
//
//    }
//
//    public class VertexConstraint {
//        public final long vertexID;
//        public final long otherVertexID;
//
//        public VertexConstraint(long vertexID, long otherVertexID) {
//            this.vertexID = vertexID;
//            this.otherVertexID = otherVertexID;
//        }
//    }


    public FittedSliceQuery getQuery(VertexCentricQuery query) {
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(!query.getVertex().isNew() && query.getVertex().hasId());

        boolean isFitted=false;
        StaticBuffer sliceStart=null, sliceEnd=null;
        boolean isStatic=false;
        int limit=query.getLimit();

        Direction dir = query.getDirection();
        RelationType rt = query.getReturnType();

        if (dir!=Direction.BOTH && rt!=RelationType.RELATION && query.numberTypes()==1) {
            int dirID = getDirID(dir,rt);
            Preconditions.checkArgument(query.numberTypes()==1);
            TitanType type = query.getTypes()[0];
            InternalType def = (InternalType)type;
            isStatic = def.isStatic(dir);

            if (query.hasConstraints() && def.getPrimaryKey().length>0) {
                Multimap<TitanType,KeyAtom<TitanType>> constraintMap = query.getConstraintMap();
                long[] primaryKey = def.getPrimaryKey();
                StandardTitanTx tx = query.getVertex().tx();

                DataOutput start = serializer.getDataOutput(DEFAULT_PRIMARY_CAPACITY, true);
                DataOutput end = serializer.getDataOutput(DEFAULT_PRIMARY_CAPACITY, true);

                IDHandler.writeEdgeType(start, type.getID(), dirID);
                IDHandler.writeEdgeType(end, type.getID(), dirID);

                int con;
                for (con=0;con<primaryKey.length;con++) {
                    TitanType kt = tx.getExistingType(primaryKey[con]);
                    Collection<KeyAtom<TitanType>> cons = constraintMap.get(kt);
                    if (cons.isEmpty()) break;
                    //Find equality constraint if exists
                    KeyAtom<TitanType> equals = null;
                    for (KeyAtom<TitanType> a : cons) if (a.getTitanPredicate()== Cmp.EQUAL) equals=a;
                    if (equals!=null) {
                        Object condition = equals.getCondition();
                        if (kt.isEdgeLabel()) {
                            long id = 0;
                            if (condition != null) id = ((TitanVertex) condition).getID();
                            VariableLong.writePositive(start, id);
                            VariableLong.writePositive(end, id);
                        } else {
                            Preconditions.checkArgument(!hasGenericDataType((TitanKey)kt));
                            start.writeObject(condition,((TitanKey) kt).getDataType());
                            end.writeObject(condition, ((TitanKey) kt).getDataType());
                        }
                    } else {
                        Preconditions.checkArgument(kt.isPropertyKey());
                        Preconditions.checkArgument(!hasGenericDataType((TitanKey)kt));
                        //Range constraint
                        Comparable lower=null, upper=null;
                        boolean lowerInc=true, upperInc=true;
                        boolean isProperInterval=true;
                        for (KeyAtom<TitanType> a : cons) {
                            if ((a.getTitanPredicate()==Cmp.GREATER_THAN || a.getTitanPredicate()==Cmp.GREATER_THAN_EQUAL) &&
                                    (lower==null || lower.compareTo(a.getCondition())<0)) {
                                lower = (Comparable) a.getCondition();
                                lowerInc = a.getTitanPredicate()==Cmp.GREATER_THAN_EQUAL;
                            } else if ((a.getTitanPredicate()==Cmp.LESS_THAN || a.getTitanPredicate()==Cmp.LESS_THAN_EQUAL) &&
                                    (upper==null || upper.compareTo(a.getCondition())>0)) {
                                upper = (Comparable) a.getCondition();
                                upperInc = a.getTitanPredicate()==Cmp.LESS_THAN_EQUAL;
                            } else {
                                isProperInterval=false;
                            }
                        }
                        if (lower!=null && upper!=null && lower.compareTo(upper)>=0) { //Interval santity check
                            isProperInterval=false;
                        }

                        if (lower != null) {
                            start.writeObject(lower,((TitanKey) kt).getDataType());
                            sliceStart = start.getStaticBuffer();
                            if (!lowerInc)
                                sliceStart = ByteBufferUtil.nextBiggerBuffer(sliceStart);
                        } else {
                            sliceStart = start.getStaticBuffer();
                        }

                        if (upper != null) {
                            end.writeObject(upper,((TitanKey) kt).getDataType());
                        }
                        sliceEnd = end.getStaticBuffer();
                        if (upperInc) sliceEnd = ByteBufferUtil.nextBiggerBuffer(sliceEnd);

                        isFitted = (con+1==constraintMap.keySet().size()) && isProperInterval;
                        break;
                    }
                }
                if (sliceStart==null) {
                    sliceStart = start.getStaticBuffer();
                    sliceEnd = FittedSliceQuery.pointRange(sliceStart);
                    isFitted = (con==constraintMap.keySet().size());
                }
            } else {
                sliceStart = IDHandler.getEdgeType(type.getID(), dirID);
                sliceEnd = FittedSliceQuery.pointRange(sliceStart);
                isFitted=!query.hasConstraints();
            }
        } else {
            int[] dirIds = getDirIDInterval(dir, rt);
            sliceStart = IDHandler.directionPlusZero(dirIds[0]);
            sliceEnd = IDHandler.directionPlusOne(dirIds[1]);
            isFitted = !query.hasType() && !query.hasConstraints() && query.isIncludeHidden();
        }
        Preconditions.checkNotNull(sliceStart);
        Preconditions.checkNotNull(sliceEnd);
        return new FittedSliceQuery(isFitted,sliceStart,sliceEnd,limit,isStatic);
    }

}