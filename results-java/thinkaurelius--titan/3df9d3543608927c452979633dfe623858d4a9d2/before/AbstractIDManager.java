package com.thinkaurelius.titan.diskstorage.idmanagement;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.time.Duration;
import com.thinkaurelius.titan.diskstorage.IDAuthority;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.configuration.Configuration;
import com.thinkaurelius.titan.diskstorage.util.BufferUtil;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import com.thinkaurelius.titan.graphdb.database.idassigner.IDBlockSizer;

import static com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration.UNIQUE_INSTANCE_ID;


/**
 * Base Class for {@link IDAuthority} implementations.
 * Handles common aspects such as maintaining the {@link IDBlockSizer} and shared configuration options
 *
 * @author Matthias Broecheler (me@matthiasb.com)
 */

public abstract class AbstractIDManager implements IDAuthority {

    /* This value can't be changed without either
      * corrupting existing ID allocations or taking
      * some additional action to prevent such
      * corruption.
      */
    protected static final long BASE_ID = 1;

    protected final Duration idApplicationWaitMS;
    protected final int randomUniqueIDLimit;

    protected final String uid;
    protected final byte[] uidBytes;

    protected final String metricsPrefix;

    private IDBlockSizer blockSizer;
    private volatile boolean isActive;

    public AbstractIDManager(Configuration config) {
        this.uid = config.get(UNIQUE_INSTANCE_ID);

        this.uidBytes = uid.getBytes();

        this.isActive = false;

        this.idApplicationWaitMS =
                config.get(GraphDatabaseConfiguration.IDAUTHORITY_WAIT);

        this.randomUniqueIDLimit =
                config.get(GraphDatabaseConfiguration.IDAUTHORITY_UNIQUEID_RETRY_COUNT);

        this.metricsPrefix = GraphDatabaseConfiguration.getSystemMetricsPrefix();
    }

    @Override
    public synchronized void setIDBlockSizer(IDBlockSizer sizer) {
        Preconditions.checkNotNull(sizer);
        if (isActive) throw new IllegalStateException("IDBlockSizer cannot be changed after IDAuthority is in use");
        this.blockSizer = sizer;
    }

    @Override
    public String getUniqueID() {
        return uid;
    }

    /**
     * Returns a byte buffer representation for the given partition id
     * @param partition
     * @return
     */
    protected StaticBuffer getPartitionKey(int partition) {
        return BufferUtil.getIntBuffer(partition);
    }

    /**
     * Returns the block size of the specified partition as determined by the configured {@link IDBlockSizer}.
     * @param partition
     * @return
     */
    protected long getBlockSize(final int partition) {
        Preconditions.checkArgument(blockSizer != null, "Blocksizer has not yet been initialized");
        isActive = true;
        long blockSize = blockSizer.getBlockSize(partition);
        Preconditions.checkArgument(blockSize>0,"Invalid block size: %s",blockSize);
        Preconditions.checkArgument(blockSize<getIdUpperBound(partition),
                "Block size [%s] cannot be larger than upper bound [%s] for partition [%s]",blockSize,getIdUpperBound(partition),partition);
        return blockSize;
    }

    protected long getIdUpperBound(final int partition) {
        Preconditions.checkArgument(blockSizer != null, "Blocksizer has not yet been initialized");
        isActive = true;
        long upperBound = blockSizer.getIdUpperBound(partition);
        Preconditions.checkArgument(upperBound>0,"Invalid upper bound: %s",upperBound);
        return upperBound;
    }

}