package com.thinkaurelius.titan.graphdb.transaction;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import com.google.common.collect.*;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.attribute.Cmp;
import com.thinkaurelius.titan.diskstorage.BackendTransaction;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.Entry;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.SliceQuery;
import com.thinkaurelius.titan.graphdb.blueprints.TitanBlueprintsTransaction;
import com.thinkaurelius.titan.graphdb.database.EdgeSerializer;
import com.thinkaurelius.titan.graphdb.database.FittedSliceQuery;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.idmanagement.IDInspector;
import com.thinkaurelius.titan.graphdb.internal.ElementLifeCycle;
import com.thinkaurelius.titan.graphdb.internal.InternalRelation;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.query.*;
import com.thinkaurelius.titan.graphdb.query.keycondition.KeyAnd;
import com.thinkaurelius.titan.graphdb.query.keycondition.KeyAtom;
import com.thinkaurelius.titan.graphdb.query.keycondition.KeyCondition;
import com.thinkaurelius.titan.graphdb.relations.AttributeUtil;
import com.thinkaurelius.titan.graphdb.relations.RelationIdentifier;
import com.thinkaurelius.titan.graphdb.relations.StandardEdge;
import com.thinkaurelius.titan.graphdb.relations.StandardProperty;
import com.thinkaurelius.titan.graphdb.transaction.addedrelations.AddedRelationsContainer;
import com.thinkaurelius.titan.graphdb.transaction.addedrelations.ConcurrentBufferAddedRelations;
import com.thinkaurelius.titan.graphdb.transaction.addedrelations.SimpleBufferAddedRelations;
import com.thinkaurelius.titan.graphdb.transaction.vertexcache.ConcurrentVertexCache;
import com.thinkaurelius.titan.graphdb.transaction.vertexcache.SimpleVertexCache;
import com.thinkaurelius.titan.graphdb.transaction.vertexcache.VertexCache;
import com.thinkaurelius.titan.graphdb.types.EdgeLabelDefinition;
import com.thinkaurelius.titan.graphdb.types.PropertyKeyDefinition;
import com.thinkaurelius.titan.graphdb.types.StandardTypeMaker;
import com.thinkaurelius.titan.graphdb.types.TitanTypeClass;
import com.thinkaurelius.titan.graphdb.types.system.SystemKey;
import com.thinkaurelius.titan.graphdb.types.system.SystemType;
import com.thinkaurelius.titan.graphdb.types.system.SystemTypeManager;
import com.thinkaurelius.titan.graphdb.types.vertices.TitanKeyVertex;
import com.thinkaurelius.titan.graphdb.types.vertices.TitanLabelVertex;
import com.thinkaurelius.titan.graphdb.util.FakeLock;
import com.thinkaurelius.titan.graphdb.util.VertexCentricEdgeIterable;
import com.thinkaurelius.titan.graphdb.vertices.CacheVertex;
import com.thinkaurelius.titan.graphdb.vertices.StandardVertex;
import com.thinkaurelius.titan.util.datastructures.Retriever;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class StandardTitanTx extends TitanBlueprintsTransaction {

    private static final Logger log = LoggerFactory.getLogger(StandardTitanTx.class);

    private static final Map<Long,InternalRelation> EMPTY_DELETED_RELATIONS = ImmutableMap.of();
    private static final ConcurrentMap<UniqueLockApplication,Lock> UNINITIALIZED_LOCKS = null;

    private static final long DEFAULT_CACHE_SIZE = 10000;

    private final StandardTitanGraph graph;
    private final TransactionConfig config;
    private final IDInspector idInspector;
    private final BackendTransaction txHandle;

    //Internal data structures
    private final VertexCache vertexCache;
    private final AtomicLong temporaryID;
    private final AddedRelationsContainer addedRelations;
    private Map<Long,InternalRelation> deletedRelations;
    private final Cache<StandardElementQuery,List<Object>> indexCache;
    private ConcurrentMap<UniqueLockApplication,Lock> uniqueLocks;

    private final Map<String,TitanType> typeCache;

    private boolean isOpen;


    public StandardTitanTx(StandardTitanGraph graph, TransactionConfig config, BackendTransaction txHandle) {
        Preconditions.checkNotNull(graph);
        Preconditions.checkArgument(graph.isOpen());
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(txHandle);
        this.graph = graph;
        this.config = config;
        this.idInspector = graph.getIDInspector();
        this.txHandle = txHandle;

        temporaryID = new AtomicLong(-1);
        CacheBuilder<StandardElementQuery,List<Object>> indexCacheBuilder = CacheBuilder.newBuilder().
                weigher(new Weigher<StandardElementQuery, List<Object>>() {
                    @Override
                    public int weigh(StandardElementQuery q, List<Object> r) {
                        return 2 + r.size();
                    }
                }).
                maximumWeight(DEFAULT_CACHE_SIZE);
        if (config.isSingleThreaded()) {
            vertexCache = new SimpleVertexCache();
            addedRelations = new SimpleBufferAddedRelations();
            indexCache = indexCacheBuilder.concurrencyLevel(1).build();
            typeCache = new HashMap<String,TitanType>();
        } else {
            vertexCache = new ConcurrentVertexCache();
            addedRelations = new ConcurrentBufferAddedRelations();
            indexCache = indexCacheBuilder.build();
            typeCache = new ConcurrentHashMap<String, TitanType>();

        }
        uniqueLocks = UNINITIALIZED_LOCKS;
        deletedRelations = EMPTY_DELETED_RELATIONS;
        this.isOpen = true;
    }

    /*
     * ------------------------------------ Utility Access Verification methods ------------------------------------
     */

    private final void verifyWriteAccess(TitanVertex... vertices) {
        if (config.isReadOnly())
            throw new UnsupportedOperationException("Cannot create new entities in read-only transaction");
        verifyAccess(vertices);
    }

    public final void verifyAccess(TitanVertex... vertices) {
        verifyOpen();
        for (TitanVertex v : vertices) {
            Preconditions.checkArgument(v instanceof InternalVertex,"Invalid vertex: " + v);
            if (!(v instanceof SystemType) && this!=((InternalVertex) v).tx())
                throw new IllegalArgumentException("The vertex or type is not associated with this transaction [" + v + "]");
            if (v.isRemoved())
                throw new IllegalArgumentException("The vertex or type has been removed [" + v + "]");
        }
    }

    private final void verifyOpen() {
        if (isClosed())
            throw new IllegalStateException("Operation cannot be executed because the enclosing transaction is closed");
    }

    /*
     * ------------------------------------ External Access ------------------------------------
     */

    public StandardTitanTx getNextTx() {
        Preconditions.checkArgument(isClosed());
        if (!config.isThreadBound()) throw new IllegalStateException("Cannot access vertex since its enclosing transaction is closed and unbound");
        else return (StandardTitanTx)graph.getCurrentThreadTx();
    }

    public TransactionConfig getConfiguration() {
        return config;
    }

    public StandardTitanGraph getGraph() {
        return graph;
    }

    public BackendTransaction getTxHandle() {
        return txHandle;
    }

    /*
     * ------------------------------------ Vertex Handling ------------------------------------
     */

    @Override
    public boolean containsVertex(final long vertexid) {
        verifyOpen();
        if (vertexCache.contains(vertexid) && !vertexCache.get(vertexid,vertexConstructor).isRemoved()) return true;
        else if (graph.containsVertexID(vertexid, txHandle)) return true;
        else return false;
    }

    @Override
    public TitanVertex getVertex(final long id) {
        verifyOpen();
        if (config.hasVerifyNodeExistence() && !containsVertex(id)) return null;
        return getExistingVertex(id);
    }

    public InternalVertex getExistingVertex(final long id) {
        //return vertex no matter what, even if deleted
        InternalVertex vertex = vertexCache.get(id,vertexConstructor);
        return vertex;
    }

    private final Retriever<Long,InternalVertex> vertexConstructor = new Retriever<Long, InternalVertex>() {
        @Override
        public InternalVertex get(Long id) {
            Preconditions.checkNotNull(id);
            Preconditions.checkArgument(id!=0);

            InternalVertex vertex = null;
            if (idInspector.isEdgeTypeID(id)) {
                if (idInspector.isEdgeTypeID(id)) {
                    vertex = new TitanLabelVertex(StandardTitanTx.this,id,ElementLifeCycle.Loaded);
                } else {
                    vertex = new TitanKeyVertex(StandardTitanTx.this,id,ElementLifeCycle.Loaded);
                }
                //If its a newly created type, add to type cache
                typeCache.put(((TitanType)vertex).getName(),(TitanType)vertex);
            } else if (idInspector.isNodeID(id)) {
                vertex = new CacheVertex(StandardTitanTx.this,id,ElementLifeCycle.Loaded);
            } else throw new IllegalArgumentException("ID could not be recognized");
            return vertex;
        }
    };




    @Override
    public TitanVertex addVertex() {
        verifyWriteAccess();
        StandardVertex vertex = new StandardVertex(this, temporaryID.decrementAndGet(),ElementLifeCycle.New);
        vertex.addProperty(SystemKey.VertexState, (byte) 0);
        if (config.hasAssignIDsImmediately()) graph.assignID(vertex);
        vertexCache.add(vertex,vertex.getID());
        return vertex;
    }


    @Override
    public Iterable<Vertex> getVertices() {
        if (!addedRelations.isEmpty()) {
            //There are possible new vertices
            List<Vertex> newVs = new ArrayList<Vertex>();
            for (InternalVertex v : vertexCache.getAll()) {
                if (v.isNew() && !(v instanceof TitanType)) newVs.add(v);
            }
            return Iterables.concat(newVs, new VertexIterable(graph, this));
        } else {
            new VertexIterable(graph, this);
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /*
     * ------------------------------------ Adding and Removing Relations ------------------------------------
     */

    public void removeRelation(InternalRelation relation) {
        //Update transaction data structures
        if (relation.isNew()) {
            addedRelations.remove(relation);
        } else {
            Preconditions.checkArgument(relation.isLoaded());
            if (deletedRelations == EMPTY_DELETED_RELATIONS) {
                if (config.isSingleThreaded()) {
                    deletedRelations = new HashMap<Long,InternalRelation>();
                } else {
                    synchronized (this) {
                        if (deletedRelations == EMPTY_DELETED_RELATIONS)
                            deletedRelations = new ConcurrentHashMap<Long, InternalRelation>();
                    }
                }
            }
            deletedRelations.put(Long.valueOf(relation.getID()),relation);
        }
        //Delete from Vertex
        for (int i=0;i<relation.getLen();i++) {
            relation.getVertex(i).removeRelation(relation);
        }
    }

    public boolean isRemovedRelation(Long relationId) {
        return deletedRelations.containsKey(relationId);
    }

    private Lock getUniquenessLock(final TitanVertex start, final TitanType type, final Object end) {
        if (config.isSingleThreaded()) return FakeLock.INSTANCE;
        if (uniqueLocks==UNINITIALIZED_LOCKS) {
            Preconditions.checkArgument(!config.isSingleThreaded());
            synchronized (this) {
                if (uniqueLocks==UNINITIALIZED_LOCKS)
                    uniqueLocks = new ConcurrentHashMap<UniqueLockApplication, Lock>();
            }
        }
        UniqueLockApplication la = new UniqueLockApplication(start,type,end);
        return uniqueLocks.putIfAbsent(la,new ReentrantLock());
    }


    @Override
    public TitanEdge addEdge(TitanVertex outVertex, TitanVertex inVertex, TitanLabel label) {
        verifyWriteAccess(outVertex, inVertex);
        Preconditions.checkNotNull(label);
        Lock uniqueLock = FakeLock.INSTANCE;
        if (config.hasVerifyUniqueness() && (label.isUnique(Direction.OUT) || label.isUnique(Direction.IN))) uniqueLock=getUniquenessLock(outVertex,label,inVertex);
        uniqueLock.lock();
        try {
            //Check uniqueness
            if (config.hasVerifyUniqueness()) {
                if (label.isUnique(Direction.OUT)) {
                    Preconditions.checkArgument(Iterables.isEmpty(query(outVertex).includeHidden().type(label).direction(Direction.OUT).relations()),
                            "An edge with the given type already exists on the out-vertex");
                }
                if (label.isUnique(Direction.IN)) {
                    Preconditions.checkArgument(Iterables.isEmpty(query(inVertex).includeHidden().type(label).direction(Direction.IN).relations()),
                            "An edge with the given type already exists on the in-vertex");
                }
            }
            StandardEdge edge = new StandardEdge(temporaryID.decrementAndGet(),label,(InternalVertex)outVertex,(InternalVertex)inVertex,ElementLifeCycle.New);
            connectRelation(edge);
            return edge;
        } finally {
            uniqueLock.unlock();
        }
    }

    private void connectRelation(InternalRelation r) {
        for (int i=0;i<r.getLen();i++) {
            boolean success = r.getVertex(i).addRelation(r);
            if (!success) throw new AssertionError("Could not connect relation: " + r);
        }
        addedRelations.add(r);
    }

    @Override
    public TitanProperty addProperty(TitanVertex vertex, TitanKey key, Object value) {
        verifyWriteAccess(vertex);
        Preconditions.checkNotNull(key);
        value = AttributeUtil.verifyAttribute(key,value);
        Lock uniqueLock = FakeLock.INSTANCE;
        if (config.hasVerifyUniqueness() && (key.isUnique(Direction.OUT) || key.isUnique(Direction.IN))) uniqueLock=getUniquenessLock(vertex,key,value);
        uniqueLock.lock();
        try {
            //Check uniqueness
            if (config.hasVerifyUniqueness()) {
                if (key.isUnique(Direction.OUT)) {
                    Preconditions.checkArgument(Iterables.isEmpty(query(vertex).includeHidden().type(key).direction(Direction.OUT).relations()),
                            "An property with the given type already exists on the vertex");
                }
                if (key.isUnique(Direction.IN)) {
                    Preconditions.checkArgument(Iterables.isEmpty(getVertices(key,value)),
                            "The given value is already used as a property");
                }
            }
            StandardProperty edge = new StandardProperty(temporaryID.decrementAndGet(),key,(InternalVertex)vertex,value,ElementLifeCycle.New);
            connectRelation(edge);
            return edge;
        } finally {
            uniqueLock.unlock();
        }
    }

    public void setProperty(TitanVertex vertex, final TitanKey key, Object value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(key.isUnique(Direction.OUT));

        Lock uniqueLock = FakeLock.INSTANCE;
        try {
            if (config.hasVerifyUniqueness()) {
                //Acquire uniqueness lock, remove and add
                uniqueLock=getUniquenessLock(vertex,key,value);
                uniqueLock.lock();
                vertex.removeProperty(key);
            } else {
                //Only delete in-memory
                InternalVertex v = (InternalVertex)vertex;
                for (InternalRelation r : v.it().getAddedRelations(new Predicate<InternalRelation>() {
                    @Override
                    public boolean apply(@Nullable InternalRelation p) {
                        return p.getType().equals(key);
                    }
                })) {
                    r.remove();
                }
            }
            addProperty(vertex,key,value);
        } finally {
            uniqueLock.unlock();
        }
    }

    @Override
    public Iterable<Edge> getEdges() {
        return new VertexCentricEdgeIterable(getVertices());
    }



    /*
     * ------------------------------------ Type Handling ------------------------------------
     */


    public TitanKey makePropertyKey(PropertyKeyDefinition definition) {
        verifyOpen();
        TitanKeyVertex prop = new TitanKeyVertex(this, temporaryID.decrementAndGet(), ElementLifeCycle.New);
        addProperty(prop, SystemKey.PropertyTypeDefinition, definition);
        addProperty(prop, SystemKey.TypeName, definition.getName());
        addProperty(prop, SystemKey.TypeClass, TitanTypeClass.KEY);
        graph.assignID(prop);
        vertexCache.add(prop,prop.getID());
        typeCache.put(definition.getName(),prop);
        return prop;
    }

    public TitanLabel makeEdgeLabel(EdgeLabelDefinition definition) {
        verifyOpen();
        TitanLabelVertex label = new TitanLabelVertex(this, temporaryID.decrementAndGet(), ElementLifeCycle.New);
        addProperty(label, SystemKey.EdgeTypeDefinition, definition);
        addProperty(label, SystemKey.TypeName, definition.getName());
        addProperty(label, SystemKey.TypeClass, TitanTypeClass.LABEL);
        graph.assignID(label);
        vertexCache.add(label, label.getID());
        typeCache.put(definition.getName(), label);
        return label;
    }

    @Override
    public boolean containsType(String name) {
        verifyOpen();
        return (typeCache.containsKey(name) || !Iterables.isEmpty(getVertices(SystemKey.TypeName,name)));
    }

    @Override
    public TitanType getType(String name) {
        verifyOpen();
        TitanType type = typeCache.get(name);
        if (type==null) type = (TitanType)Iterables.getOnlyElement(getVertices(SystemKey.TypeName,name),null);
        return type;
    }

    public TitanType getExistingType(long typeid) {
        if (idInspector.getGroupID(typeid) == SystemTypeManager.SYSTEM_TYPE_GROUP.getID()) {
            //its a systemtype
            return SystemTypeManager.getSystemEdgeType(typeid);
        } else {
            InternalVertex v = getExistingVertex(typeid);
            Preconditions.checkArgument(v instanceof TitanType,"Given id is not a type: " + typeid);
            return (TitanType)v;
        }
    }

    @Override
    public TitanKey getPropertyKey(String name) {
        TitanType et = getType(name);
        if (et == null) {
            return config.getAutoEdgeTypeMaker().makeKey(name, makeType());
        } else if (et.isPropertyKey()) {
            return (TitanKey) et;
        } else
            throw new IllegalArgumentException("The type of given name is not a key: " + name);

    }

    @Override
    public TitanLabel getEdgeLabel(String name) {
        TitanType et = getType(name);
        if (et == null) {
            return config.getAutoEdgeTypeMaker().makeLabel(name, makeType());
        } else if (et.isEdgeLabel()) {
            return (TitanLabel) et;
        } else
            throw new IllegalArgumentException("The type of given name is not a label: " + name);
    }

    @Override
    public TypeMaker makeType() {
        return new StandardTypeMaker(this);
    }

    /*
     * ------------------------------------ Query Answering ------------------------------------
     */

    public VertexCentricQueryBuilder query(TitanVertex vertex) {
        return new VertexCentricQueryBuilder((InternalVertex)vertex);
    }

    public final QueryExecutor<VertexCentricQuery,TitanRelation> edgeProcessor = new QueryExecutor<VertexCentricQuery, TitanRelation>() {

        @Override
        public boolean hasNew(final VertexCentricQuery query) {
            return ((InternalVertex)query.getVertex()).hasAddedRelations();
        }

        @Override
        public Iterator<TitanRelation> getNew(final VertexCentricQuery query) {
            return (Iterator)((InternalVertex)query.getVertex()).getAddedRelations(new Predicate<InternalRelation>() {
                @Override
                public boolean apply(@Nullable InternalRelation relation) {
                    return query.matches(relation);
                }
            }).iterator();
        }

        @Override
        public boolean isDeleted(VertexCentricQuery query, TitanRelation result) {
            return !result.isNew() && deletedRelations.containsKey(Long.valueOf(result.getID()));
        }

        @Override
        public Iterator<TitanRelation> execute(final VertexCentricQuery query) {
            final EdgeSerializer edgeSerializer = graph.getEdgeSerializer();
            FittedSliceQuery sq = edgeSerializer.getQuery(query);
            final InternalVertex v = query.getVertex();
            Iterator<Entry> iter = null;
            if (v instanceof CacheVertex) {
                CacheVertex cv = (CacheVertex)v;
                iter = ((CacheVertex) v).loadRelations(sq, new Retriever<SliceQuery, List<Entry>>() {
                    @Override
                    public List<Entry> get(SliceQuery query) {
                        return graph.edgeQuery(v.getID(), query, txHandle);
                    }
                }).iterator();
            } else {
                iter = graph.edgeQuery(v.getID(),sq,txHandle).iterator();
            }
            Iterator<TitanRelation> result = Iterators.transform(iter, new Function<Entry, TitanRelation>() {
                @Nullable
                @Override
                public TitanRelation apply(@Nullable Entry entry) {
                    return edgeSerializer.readRelation(v, entry);
                }
            });
            if (!sq.isFitted()) {
                result = Iterators.filter(result,new Predicate<TitanRelation>() {
                    @Override
                    public boolean apply(@Nullable TitanRelation titanRelation) {
                        return query.matches(titanRelation);
                    }
                });
            }
            return result;
        }

    };


    public final QueryExecutor<StandardElementQuery,TitanElement> elementProcessor = new QueryExecutor<StandardElementQuery, TitanElement>() {

        @Override
        public boolean hasNew(StandardElementQuery query) {
            return hasModifications();
        }

        @Override
        public Iterator<TitanElement> getNew(final StandardElementQuery query) {
            Preconditions.checkArgument(query.getType()== StandardElementQuery.Type.VERTEX || query.getType()== StandardElementQuery.Type.EDGE);
            if (query.getType()==StandardElementQuery.Type.VERTEX && hasModifications()) {
                //Collect all keys from the query
                final Set<TitanKey> keys = Sets.newHashSet();
                for (KeyCondition<TitanKey> atom : query.getCondition().getChildren()) {
                    keys.add(((KeyAtom<TitanKey>)atom).getKey());
                }
                Set<TitanVertex> vertices = Sets.newHashSet();
                for (TitanRelation r : addedRelations.getView(new Predicate<InternalRelation>() {
                    @Override
                    public boolean apply(@Nullable InternalRelation relation) {
                        return keys.contains(relation.getType());
                    }
                })) {
                    vertices.add(((TitanProperty)r).getVertex());
                }
                for (TitanRelation r : deletedRelations.values()) {
                    if (keys.contains(r.getType())) {
                        TitanVertex v = ((TitanProperty)r).getVertex();
                        if (!v.isRemoved()) vertices.add(v);
                    }
                }
                return (Iterator)Iterators.filter(vertices.iterator(),new Predicate<TitanVertex>() {
                    @Override
                    public boolean apply(@Nullable TitanVertex vertex) {
                        return query.matches(vertex);
                    }
                });
            } else if (query.getType()==StandardElementQuery.Type.EDGE && !addedRelations.isEmpty()) {
                return (Iterator)addedRelations.getView(new Predicate<InternalRelation>() {
                    @Override
                    public boolean apply(@Nullable InternalRelation relation) {
                        return query.matches(relation);
                    }
                }).iterator();
            } else throw new IllegalArgumentException("Unexpected type: " + query.getType());
        }

        @Override
        public boolean isDeleted(StandardElementQuery query, TitanElement result) {
            if (result.isRemoved()) return true;
            else if (query.getType()== StandardElementQuery.Type.VERTEX) {
                Preconditions.checkArgument(result instanceof InternalVertex);
                InternalVertex v = ((InternalVertex)result).it();
                if (v.hasAddedRelations() || v.hasRemovedRelations()) {
                    return query.matches(result);
                } else return false;
            } else if (query.getType()==StandardElementQuery.Type.EDGE) {
                //Loaded edges are immutable and new edges are previously filtered
                Preconditions.checkArgument(result.isLoaded() || result.isNew());
                return false;
            } else throw new IllegalArgumentException("Unexpected type: " + query.getType());
        }

        @Override
        public Iterator<TitanElement> execute(final StandardElementQuery query) {
            Iterator<TitanElement> iter = null;
            if (!query.hasIndex()) {
                log.warn("Query requires iterating over all vertices [{}]. For better performance, use indexes",query.getCondition());
                if (query.getType()==StandardElementQuery.Type.VERTEX) {
                    iter=(Iterator)getVertices().iterator();
                } else if (query.getType()==StandardElementQuery.Type.EDGE) {
                    iter=(Iterator)getEdges().iterator();
                } else throw new IllegalArgumentException("Unexpected type: "+query.getType());
                iter = Iterators.filter(iter,new Predicate<TitanElement>() {
                    @Override
                    public boolean apply(@Nullable TitanElement element) {
                        return query.matches(element);
                    }
                });
            } else {
                String index = query.getIndex();
                //Filter out everything not covered by the index
                KeyCondition<TitanKey> condition = query.getCondition();
                Preconditions.checkArgument(condition instanceof KeyAnd);
                Preconditions.checkArgument(condition.hasChildren());
                List<KeyCondition<TitanKey>> newConds = Lists.newArrayList();

                boolean needsFilter = false;
                for (KeyCondition<TitanKey> c : condition.getChildren()) {
                    KeyAtom<TitanKey> atom = (KeyAtom<TitanKey>)c;
                    if (getGraph().getIndexInformation(index).supports(atom.getKey().getDataType(),atom.getRelation()) &&
                            atom.getKey().hasIndex(index,query.getType().getElementType())) {
                        newConds.add(atom);
                    } else {
                        needsFilter = true;
                    }
                }
                final StandardElementQuery indexQuery;
                if (needsFilter) {
                    indexQuery = new StandardElementQuery(query.getType(),KeyAnd.of(newConds.toArray(new KeyAtom[newConds.size()])),index);
                } else {
                    indexQuery = query;
                }
                try {
                    iter = Iterators.transform(indexCache.get(indexQuery,new Callable<List<Object>>() {
                        @Override
                        public List<Object> call() throws Exception {
                            return graph.elementQuery(indexQuery,txHandle);
                        }
                    }).iterator(),new Function<Object, TitanElement>() {
                        @Nullable
                        @Override
                        public TitanElement apply(@Nullable Object id) {
                            Preconditions.checkNotNull(id);
                            if (id instanceof Long) return (TitanVertex)getVertex((Long)id);
                            else if (id instanceof RelationIdentifier) return (TitanElement)getEdge((RelationIdentifier)id);
                            else throw new IllegalArgumentException("Unexpected id type: " + id);
                        }
                    });
                } catch (Exception e) {
                    throw new TitanException("Could not call index",e);
                }
                if (needsFilter) {
                    iter = Iterators.filter(iter,new Predicate<TitanElement>() {
                        @Override
                        public boolean apply(@Nullable TitanElement element) {
                            return element!=null && !element.isRemoved() && query.matches(element);
                        }
                    });
                } else {
                    iter = Iterators.filter(iter,new Predicate<TitanElement>() {
                        @Override
                        public boolean apply(@Nullable TitanElement element) {
                            return element!=null && !element.isRemoved();
                        }
                    });
                }
            }
            return iter;
        }

    };

    public ElementQueryBuilder queryElements() {
        return new ElementQueryBuilder(this);
    }

    @Override
    public Iterable<TitanVertex> getVertices(TitanKey key, Object attribute) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(attribute);
        return queryElements().and(key, Cmp.EQUAL,attribute).getVertices();
    }


    @Override
    public Iterable<TitanEdge> getEdges(TitanKey key, Object attribute) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(attribute);
        return queryElements().and(key, Cmp.EQUAL,attribute).getEdges();
    }

    /*
     * ------------------------------------ Transaction State ------------------------------------
     */

    @Override
    public synchronized void commit() {
        Preconditions.checkArgument(isOpen(), "The transaction has already been closed");
        try {
            if (hasModifications()) {
                graph.save(addedRelations.getAll(), deletedRelations.values(), this);
            }
            txHandle.commit();
        } catch (StorageException e) {
            throw new TitanException("Could not commit transaction due to exception during persistence", e);
        } finally {
            close();
        }
    }

    @Override
    public synchronized void rollback() {
        Preconditions.checkArgument(isOpen(), "The transaction has already been closed");
        try {
            txHandle.abort();
        } catch (StorageException e) {
            throw new TitanException("Could not rollback transaction due to exception during persistence", e);
        } finally {
            close();
        }
    }

    private void close() {
        //TODO: release non crucial data structures to preserve memory?
        isOpen=false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public boolean isClosed() {
        return !isOpen;
    }

    @Override
    public boolean hasModifications() {
        return !addedRelations.isEmpty() || !deletedRelations.isEmpty();
    }

}