package com.thinkaurelius.faunus;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.thinkaurelius.faunus.FaunusElement.MicroElement;
import com.thinkaurelius.faunus.formats.rexster.util.ElementIdHandler;
import com.thinkaurelius.faunus.mapreduce.util.CounterMap;
import com.thinkaurelius.titan.diskstorage.ReadBuffer;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.util.ByteBufferUtil;
import com.thinkaurelius.titan.diskstorage.util.ReadByteBuffer;
import com.thinkaurelius.titan.graphdb.database.idhandling.VariableLong;
import com.thinkaurelius.titan.graphdb.database.serialize.Serializer;
import com.thinkaurelius.titan.graphdb.database.serialize.kryo.KryoSerializer;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import org.antlr.misc.MultiMap;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class FaunusSerializer {

    public static final FaunusSerializer DEFAULT_SERIALIZER = new FaunusSerializer(new KryoSerializer(true),FaunusType.DEFAULT_MANAGER);

    private final Serializer serializer;
    private final FaunusType.Manager types;

    public FaunusSerializer(Serializer serializer, FaunusType.Manager typeManager) {
        Preconditions.checkNotNull(serializer);
        Preconditions.checkNotNull(typeManager);
        this.serializer = serializer;
        this.types = typeManager;
    }

    public void writeVertex(final FaunusVertex vertex, final DataOutput out) throws IOException {
        Schema schema = new Schema();
        vertex.updateSchema(schema);
        writeElement(vertex, true, schema, out);
        writeEdges(vertex.inEdges, out, Direction.OUT, schema);
        writeEdges(vertex.outEdges, out, Direction.IN, schema);
    }

    public void readVertex(final FaunusVertex vertex, final DataInput in) throws IOException {
        Schema schema = readElement(vertex, true, null, in);
        vertex.inEdges = readEdges(in, Direction.OUT, vertex.id, schema);
        vertex.outEdges = readEdges(in, Direction.IN, vertex.id, schema);
    }

    public void writeEdge(final FaunusEdge edge, final DataOutput out) throws IOException {
        writeElement(edge,out);
        WritableUtils.writeVLong(out, edge.inVertex);
        WritableUtils.writeVLong(out, edge.outVertex);
        writeFaunusType(edge.getType(), out);
    }

    public void readEdge(final FaunusEdge edge, final DataInput in) throws IOException {
        readElement(edge,in);
        edge.inVertex = WritableUtils.readVLong(in);
        edge.outVertex = WritableUtils.readVLong(in);
        edge.setLabel(readFaunusType(in));
    }

    public void readElement(final FaunusElement element, final DataInput in) throws IOException {
        readElement(element, false, null, in);
    }

    public void writeElement(final FaunusElement element, final DataOutput out) throws IOException {
        writeElement(element, false, null, out);
    }

    public Schema readElement(final FaunusElement element, final boolean readSchema, Schema schema, final DataInput in) throws IOException {
        element.id = WritableUtils.readVLong(in);
        if (readSchema) {
            assert schema==null;
            schema=readSchema(in);
        }
        element.pathEnabled = in.readBoolean();
        if (element.pathEnabled) {
            element.paths = readElementPaths(in);
            element.microVersion = (element instanceof FaunusVertex) ? new FaunusVertex.MicroVertex(element.id) : new FaunusEdge.MicroEdge(element.id);
        } else
            element.pathCounter = WritableUtils.readVLong(in);
        element.properties = readProperties(schema, in);
        return schema;
    }

    private void writeElement(final FaunusElement element, final boolean writeSchema, final Schema schema, final DataOutput out) throws IOException {
        WritableUtils.writeVLong(out, element.id);
        if (writeSchema) {
            assert schema!=null;
            schema.writeSchema(out);
        }
        out.writeBoolean(element.pathEnabled);
        if (element.pathEnabled)
            writeElementPaths(element.paths, out);
        else
            WritableUtils.writeVLong(out, element.pathCounter);
        writeProperties(element.properties, schema, out);
    }

    private void writeProperties(final Multimap<FaunusType, Object> properties, final Schema schema, final DataOutput out) throws IOException {
        if (properties.isEmpty())
            WritableUtils.writeVInt(out, 0);
        else {
//            WritableUtils.writeVInt(out, properties.size());
            final com.thinkaurelius.titan.graphdb.database.serialize.DataOutput o = serializer.getDataOutput(properties.size()*40, true);
            for (final Map.Entry<FaunusType, Object> entry : properties.entries()) {
                if (schema==null) writeFaunusType(entry.getKey(),o);
                else VariableLong.writePositive(o,schema.getTypeId(entry.getKey()));
                o.writeClassAndObject(entry.getValue());
            }
            final StaticBuffer buffer = o.getStaticBuffer();
            WritableUtils.writeVInt(out, buffer.length());
            out.write(ByteBufferUtil.getArray(buffer.asByteBuffer()));
        }
    }

    private Multimap<FaunusType, Object> readProperties(final Schema schema, final DataInput in) throws IOException {
        final int numPropertyBytes = WritableUtils.readVInt(in);
        if (numPropertyBytes == 0)
            return FaunusElement.NO_PROPERTIES;
        else {
            final Multimap<FaunusType, Object> properties = HashMultimap.create();
//            byte[] bytes = new byte[WritableUtils.readVInt(in)];
            byte[] bytes = new byte[numPropertyBytes];
            in.readFully(bytes);
            final ReadBuffer buffer = new ReadByteBuffer(bytes);
            while (buffer.hasRemaining()) {
                FaunusType type;
                if (schema==null) type = readFaunusType(buffer);
                else type = schema.getType(VariableLong.readPositive(buffer));
                Object value = serializer.readClassAndObject(buffer);
                properties.put(type,value);
            }
            return properties;
        }
    }

    public ListMultimap<FaunusType, FaunusEdge> readEdges(final DataInput in, final Direction idToRead, final long otherId, final Schema schema) throws IOException {
        final ListMultimap<FaunusType,FaunusEdge> edges = ArrayListMultimap.create();
        int edgeTypes = WritableUtils.readVInt(in);
        for (int i = 0; i < edgeTypes; i++) {
            FaunusType type = schema.getType(WritableUtils.readVLong(in));
            final int size = WritableUtils.readVInt(in);
            for (int j = 0; j < size; j++) {
                final FaunusEdge edge = new FaunusEdge();
                readElement(edge,false,schema,in);
                edge.setLabel(type);
                long vertexId = WritableUtils.readVLong(in);
                switch(idToRead) {
                    case IN:
                        edge.inVertex = vertexId;
                        edge.outVertex = otherId;
                        break;
                    case OUT:
                        edge.outVertex = vertexId;
                        edge.inVertex = otherId;
                        break;
                    default: throw ExceptionFactory.bothIsNotSupported();
                }
                edges.put(type, edge);
            }
        }
        return edges;
    }

    public void writeEdges(final ListMultimap<FaunusType, FaunusEdge> edges, final DataOutput out, final Direction idToWrite, final Schema schema) throws IOException {
        WritableUtils.writeVInt(out, edges.keySet().size());
        for (FaunusType type : edges.keySet()) {
            List<FaunusEdge> subset = edges.get(type);
            WritableUtils.writeVLong(out, schema.getTypeId(type));
            WritableUtils.writeVInt(out, subset.size());
            for (final FaunusEdge edge : subset) {
                writeElement(edge,false,schema,out);
                WritableUtils.writeVLong(out, edge.getVertexId(idToWrite));
            }
        }
    }

    private void writeElementPaths(final List<List<MicroElement>> paths, final DataOutput out) throws IOException {
        if (null == paths) {
            WritableUtils.writeVInt(out, 0);
        } else {
            WritableUtils.writeVInt(out, paths.size());
            for (final List<MicroElement> path : paths) {
                WritableUtils.writeVInt(out, path.size());
                for (MicroElement element : path) {
                    if (element instanceof FaunusVertex.MicroVertex)
                        out.writeChar('v');
                    else
                        out.writeChar('e');
                    WritableUtils.writeVLong(out, element.getId());
                }
            }
        }
    }

    private List<List<MicroElement>> readElementPaths(final DataInput in) throws IOException {
        int pathsSize = WritableUtils.readVInt(in);
        if (pathsSize == 0)
            return new ArrayList<List<MicroElement>>();
        else {
            final List<List<MicroElement>> paths = new ArrayList<List<MicroElement>>(pathsSize);
            for (int i = 0; i < pathsSize; i++) {
                int pathSize = WritableUtils.readVInt(in);
                final List<MicroElement> path = new ArrayList<MicroElement>(pathSize);
                for (int j = 0; j < pathSize; j++) {
                    char type = in.readChar();
                    if (type == 'v')
                        path.add(new FaunusVertex.MicroVertex(WritableUtils.readVLong(in)));
                    else
                        path.add(new FaunusEdge.MicroEdge(WritableUtils.readVLong(in)));
                }
                paths.add(path);
            }
            return paths;
        }
    }

    private void writeFaunusType(final FaunusType type, final DataOutput out) throws IOException {
        out.writeUTF(type.getName());
    }

    private FaunusType readFaunusType(final DataInput in) throws IOException {
        return types.get(in.readUTF());
    }

    private void writeFaunusType(final FaunusType type, final com.thinkaurelius.titan.graphdb.database.serialize.DataOutput out) throws IOException {
        out.writeObjectNotNull(type.getName());
    }

    private FaunusType readFaunusType(final ReadBuffer buffer) throws IOException {
        return types.get(serializer.readObjectNotNull(buffer, String.class));
    }

    class Schema {

        private final BiMap<FaunusType,Long> localTypes;
        private long count = 1;

        private Schema() {
            this(8);
        }

        private Schema(int size) {
            localTypes = HashBiMap.create(size);
        }

        void add(String type) {
            this.add(types.get(type));
        }

        void add(FaunusType type) {
            if (!localTypes.containsKey(type)) localTypes.put(type,count++);
        }

        void addAll(Iterable<FaunusType> types) {
            for (FaunusType type : types) add(type);
        }

        long getTypeId(FaunusType type) {
            Long id = localTypes.get(type);
            Preconditions.checkArgument(id!=null,"Type is not part of the schema: " + type);
            return id;
        }

        FaunusType getType(long id) {
            FaunusType type = localTypes.inverse().get(id);
            Preconditions.checkArgument(type!=null,"Type is not part of the schema: " + id);
            return type;
        }

        private void add(FaunusType type, long index) {
            Preconditions.checkArgument(!localTypes.containsValue(index));
            localTypes.put(type, index);
            count = index+1;
        }

        private void writeSchema(final DataOutput out) throws IOException  {
            WritableUtils.writeVInt(out, localTypes.size());
            for (Map.Entry<FaunusType,Long> entry : localTypes.entrySet()) {
                writeFaunusType(entry.getKey(),out);
                WritableUtils.writeVLong(out, entry.getValue());
            }
        }

    }

    private Schema readSchema(final DataInput in) throws IOException {
        int size = WritableUtils.readVInt(in);
        Schema schema = new Schema(size);
        for (int i=0;i<size;i++) {
            schema.add(readFaunusType(in),WritableUtils.readVLong(in));
        }
        return schema;
    }



    static {
        WritableComparator.define(FaunusElement.class, new Comparator());
    }

    public static class Comparator extends WritableComparator {
        public Comparator() {
            super(FaunusElement.class);
        }

        @Override
        public int compare(final byte[] element1, final int start1, final int length1, final byte[] element2, final int start2, final int length2) {
            try {
                return Long.valueOf(readVLong(element1, start1)).compareTo(readVLong(element2, start2));
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        public int compare(final WritableComparable a, final WritableComparable b) {
            if (a instanceof FaunusElement && b instanceof FaunusElement)
                return ((Long) (((FaunusElement) a).getIdAsLong())).compareTo(((FaunusElement) b).getIdAsLong());
            else
                return super.compare(a, b);
        }
    }

    //################################################
    // Serialization for vanilla Blueprints
    //################################################

    public void writeVertex(final Vertex vertex, final ElementIdHandler elementIdHandler, final DataOutput out) throws IOException {
        Schema schema = new Schema();
        //Convert properties and update schema
        Multimap<FaunusType,Object> properties = getProperties(vertex);
        for (FaunusType type : properties.keySet()) schema.add(type);
        for (Edge edge : vertex.getEdges(Direction.BOTH)) {
            schema.add(edge.getLabel());
            for (String key : edge.getPropertyKeys()) schema.add(key);
        }

        WritableUtils.writeVLong(out, elementIdHandler.convertIdentifier(vertex));
        schema.writeSchema(out);
        out.writeBoolean(false);
        WritableUtils.writeVLong(out, 0);
        writeProperties(properties,schema,out);
        writeEdges(vertex, Direction.IN, elementIdHandler, schema, out);
        writeEdges(vertex, Direction.OUT, elementIdHandler, schema, out);

    }

    private Multimap<FaunusType,Object> getProperties(Element element) {
        Multimap<FaunusType,Object> properties = HashMultimap.create();
        for (String key : element.getPropertyKeys()) {
            FaunusType type = types.get(key);
            properties.put(type,element.getProperty(key));
        }
        return properties;
    }

    private void writeEdges(final Vertex vertex, final Direction direction, final ElementIdHandler elementIdHandler,
                            final Schema schema, final DataOutput out) throws IOException {
        final Multiset<String> labelCount = HashMultiset.create();
        for (final Edge edge : vertex.getEdges(direction)) {
            labelCount.add(edge.getLabel());
        }
        WritableUtils.writeVInt(out, labelCount.elementSet().size());
        for (String label : labelCount.elementSet()) {
            FaunusType type = types.get(label);
            WritableUtils.writeVLong(out, schema.getTypeId(type));
            WritableUtils.writeVInt(out, labelCount.count(label));
            for (final Edge edge : vertex.getEdges(direction, label)) {
                WritableUtils.writeVLong(out, elementIdHandler.convertIdentifier(edge));
                out.writeBoolean(false);
                WritableUtils.writeVLong(out, 0);
                writeProperties(getProperties(edge),schema, out);
                WritableUtils.writeVLong(out, elementIdHandler.convertIdentifier(edge.getVertex(direction.opposite())));
            }
        }
    }

}