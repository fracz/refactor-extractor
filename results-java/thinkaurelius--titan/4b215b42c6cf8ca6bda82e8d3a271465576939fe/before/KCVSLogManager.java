package com.thinkaurelius.titan.diskstorage.log.kcvs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.configuration.ConfigOption;
import com.thinkaurelius.titan.diskstorage.configuration.Configuration;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.*;
import com.thinkaurelius.titan.diskstorage.log.Log;
import com.thinkaurelius.titan.diskstorage.log.LogManager;
import com.thinkaurelius.titan.diskstorage.log.ReadMarker;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import com.thinkaurelius.titan.graphdb.database.serialize.Serializer;
import com.thinkaurelius.titan.graphdb.database.serialize.StandardSerializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration.IDS_PARTITION;
import static com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration.LOG_NS;

/**
 * Implementation of {@link LogManager} against an arbitrary {@link KeyColumnValueStoreManager}. Issues {@link Log} instances
 * which wrap around a {@link KeyColumnValueStore}.
 *
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class KCVSLogManager implements LogManager {

    public static final ConfigOption<Boolean> LOG_FIXED_PARTITION = new ConfigOption<Boolean>(LOG_NS,"fixed-partition",
            "Whether all log entries are written to one fixed partition even if the backend store is partitioned." +
                    "This can cause imbalanced loads and should only be used on low volume logs",
            ConfigOption.Type.GLOBAL_OFFLINE, false);

    /**
     * Configuration of this log manager
     */
    private final Configuration configuration;
    /**
     * Store Manager against which to open the {@link KeyColumnValueStore}s to wrap the {@link KCVSLog} around.
     */
    final KeyColumnValueStoreManager storeManager;
    /**
     * Id which uniquely identifies this instance. Also see {@link GraphDatabaseConfiguration#UNIQUE_INSTANCE_ID}.
     */
    final String senderId;

    /**
     * The number of first bits of the key that identifies a partition. If this number is X then there are 2^X different
     * partition blocks each of which is identified by a partition id.
     */
    final int partitionBitWidth;
    /**
     * A collection of partition ids to which the logs write in round-robin fashion.
     */
    final int[] defaultWritePartitionIds;
    /**
     * A collection of partition ids from which the readers will read concurrently.
     */
    final int[] readPartitionIds;
    /**
     * Serializer used to (de)-serialize the log messages
     */
    final Serializer serializer;

    /**
     * Keeps track of all open logs
     */
    private final Map<String,KCVSLog> openLogs;

    /**
     * Opens a log manager against the provided KCVS store with the given configuration.
     * @param storeManager
     * @param config
     */
    public KCVSLogManager(final KeyColumnValueStoreManager storeManager, final Configuration config) {
        this(storeManager, config, null);
    }

    /**
     * Opens a log manager against the provided KCVS store with the given configuration. Also provided is a list
     * of read-partition-ids. These only apply when readers are registered against an opened log. In that case,
     * the readers only read from the provided list of partition ids.
     * @param storeManager
     * @param config
     * @param readPartitionIds
     */
    public KCVSLogManager(final KeyColumnValueStoreManager storeManager, final Configuration config,
                          final int[] readPartitionIds) {
        Preconditions.checkArgument(storeManager!=null && config!=null);
        this.storeManager = storeManager;
        this.configuration = config;
        openLogs = new HashMap<String, KCVSLog>();

        this.senderId=config.get(GraphDatabaseConfiguration.UNIQUE_INSTANCE_ID);
        Preconditions.checkNotNull(senderId);

        if (config.get(IDS_PARTITION)) {
            //TODO in Titan0.6: Use the configured system-level partitionBitWdith
            this.partitionBitWidth=5; //32 partitions by default
        } else {
            this.partitionBitWidth=0;
        }
        Preconditions.checkArgument(partitionBitWidth>=0 && partitionBitWidth<32);

        //Partitioning
        if (config.get(IDS_PARTITION) && !config.get(LOG_FIXED_PARTITION)) {
            //TODO in Titan0.6: Use only the local ids (if such exist upon inspecting storeManager features). Reuse between VertexIdAssigner
            this.defaultWritePartitionIds=new int[(1<<partitionBitWidth)];
            for (int i=0;i<(1<<partitionBitWidth);i++) defaultWritePartitionIds[i]=i;
            if (readPartitionIds!=null && readPartitionIds.length>0) {
                for (int readPartitionId : readPartitionIds) {
                    checkValidPartitionId(readPartitionId,partitionBitWidth);
                }
                this.readPartitionIds = Arrays.copyOf(readPartitionIds,readPartitionIds.length);
            } else {
                this.readPartitionIds = Arrays.copyOf(defaultWritePartitionIds,defaultWritePartitionIds.length);
            }
        } else {
            this.defaultWritePartitionIds=new int[]{0};
            Preconditions.checkArgument(readPartitionIds==null || (readPartitionIds.length==0 && readPartitionIds[0]==0),
                    "Cannot configure read partition ids on unpartitioned backend or with fixed partitions enabled");
            this.readPartitionIds=new int[]{0};
        }

        this.serializer = new StandardSerializer(false);
    }

    private static void checkValidPartitionId(int partitionId, int partitionBitWidth) {
        Preconditions.checkArgument(partitionId>=0 && partitionId<(1<<partitionBitWidth));
    }

    @Override
    public synchronized Log openLog(final String name, ReadMarker readMarker) throws StorageException {
        if (openLogs.containsKey(name)) return openLogs.get(name);
        KCVSLog log = new KCVSLog(name,this,storeManager.openDatabase(name),readMarker,configuration);
        openLogs.put(name,log);
        return log;
    }

    /**
     * Must be triggered by a particular {@link KCVSLog} when it is closed so that this log can be removed from the list
     * of open logs.
     * @param log
     */
    synchronized void closedLog(KCVSLog log) {
        KCVSLog l = openLogs.remove(log.getName());
        assert l==log;
    }

    @Override
    public synchronized void close() throws StorageException {
        /* Copying the map is necessary to avoid ConcurrentModificationException.
         * The path to ConcurrentModificationException in the absence of a copy is
         * log.close() -> manager.closedLog(log) -> openLogs.remove(log.getName()).
         */
        for (KCVSLog log : ImmutableMap.copyOf(openLogs).values()) log.close();
    }

}