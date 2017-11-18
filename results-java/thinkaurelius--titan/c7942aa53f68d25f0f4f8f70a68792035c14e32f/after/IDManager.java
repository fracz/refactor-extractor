package com.thinkaurelius.titan.graphdb.idmanagement;


import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.util.BufferUtil;
import com.thinkaurelius.titan.graphdb.database.idhandling.VariableLong;

/**
 * Handles the allocation of ids based on the type of element
 * Responsible for the bit-wise pattern of Titan's internal id scheme.
 *
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class IDManager {

    /**
     *bit mask- Description (+ indicates defined type, * indicates proper & defined type)
     *
     *      0 - + User created Vertex
     *    000 -     * Normal vertices
     *    010 -     * Partitioned vertices
     *    100 -     * Unmodifiable (e.g. TTL'ed) vertices
     *    110 -     Reserved for additional vertex type
     *      1 - + Hidden
     *     11 -     * Hidden (user created/triggered) Vertex [for later]
     *     01 -     + Schema related vertices
     *    101 -         + Schema Type vertices
     *   0101 -             + Relation Type vertices
     *  00101 -                 + Property Key
     * 000101 -                     * User Property Key
     * 100101 -                     * System Property Key
     *  10101 -                 + Edge Label
     * 010101 -                     * User Edge Label
     * 110101 -                     * System Edge Label
     *   1101 -             Other Type vertices
     *  01101 -                 + Vertex Label
     *    001 -         Non-Type vertices
     *   1001 -             * Generic Schema Vertex
     *   0001 -             Reserved for future
     *
     *
     */
    public enum VertexIDType {
        UserVertex {
            @Override
            final long offset() {
                return 1l;
            }

            @Override
            final long suffix() {
                return 0l;
            } // 0b

            @Override
            final boolean isProper() {
                return false;
            }
        },
        NormalVertex {
            @Override
            final long offset() {
                return 3l;
            }

            @Override
            final long suffix() {
                return 0l;
            } // 000b

            @Override
            final boolean isProper() {
                return true;
            }
        },
        PartitionedVertex {
            @Override
            final long offset() {
                return 3l;
            }

            @Override
            final long suffix() {
                return 2l;
            } // 010b

            @Override
            final boolean isProper() {
                return true;
            }
        },
        UnmodifiableVertex {
            @Override
            final long offset() {
                return 3l;
            }

            @Override
            final long suffix() {
                return 4l;
            } // 100b

            @Override
            final boolean isProper() {
                return true;
            }
        },

        Hidden {
            @Override
            final long offset() {
                return 1l;
            }

            @Override
            final long suffix() {
                return 1l;
            } // 1b

            @Override
            final boolean isProper() {
                return false;
            }
        },
        HiddenVertex {
            @Override
            final long offset() {
                return 2l;
            }

            @Override
            final long suffix() {
                return 3l;
            } // 11b

            @Override
            final boolean isProper() {
                return true;
            }
        },
        Schema {
            @Override
            final long offset() {
                return 2l;
            }

            @Override
            final long suffix() {
                return 1l;
            } // 01b

            @Override
            final boolean isProper() {
                return false;
            }
        },
        SchemaType {
            @Override
            final long offset() {
                return 3l;
            }

            @Override
            final long suffix() {
                return 5l;
            } // 101b

            @Override
            final boolean isProper() {
                return false;
            }
        },
        RelationType {
            @Override
            final long offset() {
                return 4l;
            }

            @Override
            final long suffix() {
                return 5l;
            } // 0101b

            @Override
            final boolean isProper() {
                return false;
            }
        },
        PropertyKey {
            @Override
            final long offset() {
                return 5l;
            }

            @Override
            final long suffix() {
                return 5l;
            }    // 00101b

            @Override
            final boolean isProper() {
                return false;
            }
        },
        UserPropertyKey {
            @Override
            final long offset() {
                return 6l;
            }

            @Override
            final long suffix() {
                return 5l;
            }    // 000101b

            @Override
            final boolean isProper() {
                return true;
            }
        },
        SystemPropertyKey {
            @Override
            final long offset() {
                return 6l;
            }

            @Override
            final long suffix() {
                return 37l;
            }    // 100101b

            @Override
            final boolean isProper() {
                return true;
            }
        },
        EdgeLabel {
            @Override
            final long offset() {
                return 5l;
            }

            @Override
            final long suffix() {
                return 21l;
            } // 10101b

            @Override
            final boolean isProper() {
                return false;
            }
        },
        UserEdgeLabel {
            @Override
            final long offset() {
                return 6l;
            }

            @Override
            final long suffix() {
                return 21l;
            } // 010101b

            @Override
            final boolean isProper() {
                return true;
            }
        },
        SystemEdgeLabel {
            @Override
            final long offset() {
                return 6l;
            }

            @Override
            final long suffix() {
                return 53l;
            } // 110101b

            @Override
            final boolean isProper() {
                return true;
            }
        },

        GenericSchemaType {
            @Override
            final long offset() {
                return 4l;
            }

            @Override
            final long suffix() {
                return 9l;
            }    // 1001b

            @Override
            final boolean isProper() {
                return true;
            }
        };

        abstract long offset();

        abstract long suffix();

        abstract boolean isProper();

        public final long addPadding(long count) {
            assert offset()>0;
            Preconditions.checkArgument(count>0 && count<(1l<<(TOTAL_BITS-offset())),"Count out of range for type [%s]: %s",this,count);
            return (count << offset()) | suffix();
        }

        public final long removePadding(long id) {
            return id >>> offset();
        }

        public final boolean is(long id) {
            return (id & ((1l << offset()) - 1)) == suffix();
        }

        public final boolean isSubType(VertexIDType type) {
            return is(type.suffix());
        }
    }

    /**
     * Number of bits that need to be reserved from the type ids for storing additional information during serialization
     */
    public static final int TYPE_LEN_RESERVE = 3;

    /**
     * Total number of bits available to a Titan assigned id
     * We use only 63 bits to make sure that all ids are positive
     *
     */
    private static final long TOTAL_BITS = Long.SIZE-1;

    /**
     * Maximum number of bits that can be used for the partition prefix of an id
     */
    private static final long MAX_PARTITION_BITS = 16;
    /**
     * Default number of bits used for the partition prefix. 0 means there is no partition prefix
     */
    private static final long DEFAULT_PARTITION_BITS = 0;
    /**
     * The padding bit with for user vertices
     */
    public static final long USERVERTEX_PADDING_BITWIDTH = VertexIDType.NormalVertex.offset();

    /**
     * The maximum number of padding bits of any type
     */
    public static final long MAX_PADDING_BITWIDTH = VertexIDType.UserEdgeLabel.offset();

    /**
     * Bound on the maximum count for a schema id
     */
    private static final long SCHEMA_COUNT_BOUND = (1l << (TOTAL_BITS - MAX_PADDING_BITWIDTH - TYPE_LEN_RESERVE));


    @SuppressWarnings("unused")
    private final long partitionBits;
    private final long partitionOffset;
    private final long partitionIDBound;

    private final long relationCountBound;
    private final long vertexCountBound;


    public IDManager(long partitionBits) {
        Preconditions.checkArgument(partitionBits >= 0);
        Preconditions.checkArgument(partitionBits <= MAX_PARTITION_BITS,
                "Partition bits can be at most %s bits", MAX_PARTITION_BITS);
        this.partitionBits = partitionBits;

        partitionIDBound = (1l << (partitionBits));

        relationCountBound = partitionBits==0?Long.MAX_VALUE:(1l << (TOTAL_BITS - partitionBits));
        assert VertexIDType.NormalVertex.offset()>0;
        vertexCountBound = (1l << (TOTAL_BITS - partitionBits - USERVERTEX_PADDING_BITWIDTH));


        partitionOffset = Long.SIZE - partitionBits;
    }

    public IDManager() {
        this(DEFAULT_PARTITION_BITS);
    }

    public long getPartitionBound() {
        return partitionIDBound;
    }

    /* ########################################################
                   User Relations and Vertices
       ########################################################  */

     /*		--- TitanElement id bit format ---
      *  [ 0 | count | partition | ID padding (if any) ]
     */

    private long constructId(long count, long partition, VertexIDType type) {
        Preconditions.checkArgument(partition<partitionIDBound && partition>=0,"Invalid partition: %s",partition);
        Preconditions.checkArgument(count>=0);
        Preconditions.checkArgument(VariableLong.unsignedBitLength(count)+partitionBits+
                (type==null?0:type.offset())<=TOTAL_BITS);
        Preconditions.checkArgument(type==null || type.isProper());
        long id = (count<<partitionBits)+partition;
        if (type!=null) id = type.addPadding(id);
        return id;
    }

    private static VertexIDType getVertexIDType(long vertexid) {
        VertexIDType type=null;
        if (VertexIDType.NormalVertex.is(vertexid)) type=VertexIDType.NormalVertex;
        else if (VertexIDType.PartitionedVertex.is(vertexid)) type=VertexIDType.PartitionedVertex;
        else if (VertexIDType.UnmodifiableVertex.is(vertexid)) type=VertexIDType.UnmodifiableVertex;
        Preconditions.checkArgument(type!=null,"Vertex id has unrecognized type: %s",vertexid);
        return type;
    }

    public long getPartitionId(long vertexid) {
        assert getVertexIDType(vertexid)!=null;
        long partition = (vertexid>>>USERVERTEX_PADDING_BITWIDTH) & (partitionIDBound-1);
        assert partition>=0;
        return partition;
    }

    public StaticBuffer getKey(long vertexid) {
        if (VertexIDType.Schema.is(vertexid)) {
            //No partition for schema vertices
            return BufferUtil.getLongBuffer(vertexid);
        } else {
            VertexIDType type = getVertexIDType(vertexid);
            long partition = getPartitionId(vertexid);
            long count = vertexid>>>(partitionBits+USERVERTEX_PADDING_BITWIDTH);
            assert count>0;
            long keyid = (partition<<partitionOffset) | type.addPadding(count);
            return BufferUtil.getLongBuffer(keyid);
        }
    }

    public long getKeyID(StaticBuffer b) {
        long value = b.getLong(0);
        if (VertexIDType.Schema.is(value)) {
            return value;
        } else {
            VertexIDType type = getVertexIDType(value);
            long partition = partitionOffset<Long.SIZE?value>>>partitionOffset:0;
            long count = (value>>>USERVERTEX_PADDING_BITWIDTH) & ((1l<<(partitionOffset-USERVERTEX_PADDING_BITWIDTH))-1);
            return constructId(count,partition,type);
        }
    }

    public long getRelationID(long count, long partition) {
        Preconditions.checkArgument(count>0 && count< relationCountBound,"Invalid count for bound: %s", relationCountBound);
        return constructId(count, partition, null);
    }


    public long getVertexID(long count, long partition, VertexIDType vertexType) {
        Preconditions.checkArgument(VertexIDType.UserVertex.is(vertexType.suffix()),"Not a user vertex type: %s",vertexType);
        Preconditions.checkArgument(count>0 && count<vertexCountBound,"Invalid count for bound: %s", vertexCountBound);
        return constructId(count, partition, vertexType);
    }

    public long getRelationCountBound() {
        return relationCountBound;
    }

    public long getVertexCountBound() {
        return vertexCountBound;
    }

    /*

    Temporary ids are negative and don't have partitions

     */

    public static long getTemporaryRelationID(long count) {
        return makeTemporary(count);
    }

    public static long getTemporaryVertexID(VertexIDType type, long count) {
        Preconditions.checkArgument(type.isProper(),"Invalid vertex id type: %s",type);
        return makeTemporary(type.addPadding(count));
    }

    private static long makeTemporary(long id) {
        Preconditions.checkArgument(id>0);
        return (1l<<63) | id; //make negative but preserve bit pattern
    }

    public static boolean isTemporary(long id) {
        return id<0;
    }

    /* ########################################################
               Schema Vertices
   ########################################################  */

    /* --- TitanRelation Type id bit format ---
      *  [ 0 | count | ID padding ]
      *  (there is no partition)
     */


    private static void checkSchemaTypeId(VertexIDType type, long count) {
        Preconditions.checkArgument(VertexIDType.Schema.is(type.suffix()),"Expected schema vertex but got: %s",type);
        Preconditions.checkArgument(type.isProper(),"Expected proper type but got: %s",type);
        Preconditions.checkArgument(count > 0 && count < SCHEMA_COUNT_BOUND,
                "Invalid id [%s] for type [%s] bound: %s", count, type, SCHEMA_COUNT_BOUND);
    }

    public static long getSchemaId(VertexIDType type, long count) {
        checkSchemaTypeId(type,count);
        return type.addPadding(count);
    }

    private static boolean isProperRelationType(long id) {
        return VertexIDType.UserEdgeLabel.is(id) || VertexIDType.SystemEdgeLabel.is(id)
                || VertexIDType.UserPropertyKey.is(id) || VertexIDType.SystemPropertyKey.is(id);
    }

    public static long stripEntireRelationTypePadding(long id) {
        Preconditions.checkArgument(isProperRelationType(id));
        return VertexIDType.UserEdgeLabel.removePadding(id);
    }

    public static long stripRelationTypePadding(long id) {
        Preconditions.checkArgument(isProperRelationType(id));
        return VertexIDType.RelationType.removePadding(id);
    }

    public static long addRelationTypePadding(long id) {
        long typeid = VertexIDType.RelationType.addPadding(id);
        Preconditions.checkArgument(isProperRelationType(typeid));
        return typeid;
    }

    public static boolean isSystemRelationTypeId(long id) {
        return VertexIDType.SystemEdgeLabel.is(id) || VertexIDType.SystemPropertyKey.is(id);
    }

    public static long getSchemaCountBound() {
        return SCHEMA_COUNT_BOUND;
    }


    /* ########################################################
               Inspector
   ########################################################  */


    private final IDInspector inspector = new IDInspector() {

        @Override
        public final long getPartitionId(long id) {
            return IDManager.this.getPartitionId(id);
        }

        @Override
        public final boolean isSchemaVertexId(long id) {
            return VertexIDType.Schema.is(id);
        }

        @Override
        public final boolean isRelationTypeId(long id) {
            return VertexIDType.RelationType.is(id);
        }

        @Override
        public final boolean isEdgeLabelId(long id) {
            return VertexIDType.EdgeLabel.is(id);
        }

        @Override
        public final boolean isPropertyKeyId(long id) {
            return VertexIDType.PropertyKey.is(id);
        }

        @Override
        public boolean isSystemRelationTypeId(long id) {
            return IDManager.isSystemRelationTypeId(id);
        }

        @Override
        public boolean isGenericSchemaVertexId(long id) {
            return VertexIDType.GenericSchemaType.is(id);
        }



        @Override
        public final boolean isUserVertexId(long id) {
            return VertexIDType.UserVertex.is(id);
        }

        @Override
        public boolean isUnmodifiableVertex(long id) {
            return VertexIDType.UnmodifiableVertex.is(id);
        }

        @Override
        public boolean isPartitionedVertex(long id) {
            return VertexIDType.PartitionedVertex.is(id);
        }

    };

    public IDInspector getIdInspector() {
        return inspector;
    }

}