package com.thinkaurelius.titan.graphdb.transaction;

import static com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration.TITAN_NS;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.core.DefaultTypeMaker;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TransactionBuilder;
import com.thinkaurelius.titan.core.UserModifiableConfiguration;
import com.thinkaurelius.titan.diskstorage.TransactionHandleConfig;
import com.thinkaurelius.titan.diskstorage.configuration.BasicConfiguration;
import com.thinkaurelius.titan.diskstorage.configuration.BasicConfiguration.Restriction;
import com.thinkaurelius.titan.diskstorage.configuration.ConfigOption;
import com.thinkaurelius.titan.diskstorage.configuration.Configuration;
import com.thinkaurelius.titan.diskstorage.util.StandardTransactionConfig;
import com.thinkaurelius.titan.diskstorage.util.TimestampProvider;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;


/**
 * Used to configure a {@link com.thinkaurelius.titan.core.TitanTransaction}.
 *
 * @author Matthias Br&ouml;cheler (me@matthiasb.com);
 * @see com.thinkaurelius.titan.core.TitanTransaction
 */
public class StandardTransactionBuilder implements TransactionConfiguration, TransactionBuilder {

    private boolean isReadOnly = false;

    private boolean assignIDsImmediately = false;

    private DefaultTypeMaker defaultTypeMaker;

    private boolean verifyExternalVertexExistence = true;

    private boolean verifyInternalVertexExistence = false;

    private boolean verifyUniqueness = true;

    private boolean acquireLocks = true;

    private boolean propertyPrefetching = true;

    private boolean singleThreaded = false;

    private boolean threadBound = false;

    private int vertexCacheSize;

    private long indexCacheWeight;

    private Long timestamp = null;

    private String metricsPrefix;

    private TimestampProvider timestampProvider;

    private final UserModifiableConfiguration storageConfiguration;

    private final StandardTitanGraph graph;

    /**
     * Constructs a new TitanTransaction configuration with default configuration parameters.
     */
    public StandardTransactionBuilder(GraphDatabaseConfiguration graphConfig, StandardTitanGraph graph) {
        Preconditions.checkNotNull(graphConfig);
        Preconditions.checkNotNull(graph);
        this.graph = graph;
        this.defaultTypeMaker = graphConfig.getDefaultTypeMaker();
        this.assignIDsImmediately = graphConfig.hasFlushIDs();
        this.metricsPrefix = graphConfig.getMetricsPrefix();
        this.propertyPrefetching = graphConfig.hasPropertyPrefetching();
        this.timestampProvider = graphConfig.getTimestampProvider();
        this.storageConfiguration = TitanFactory.buildConfiguration();
        if (graphConfig.isReadOnly()) readOnly();
        setCacheSize(graphConfig.getTxCacheSize());
        if (graphConfig.isBatchLoading()) enableBatchLoading();
    }

    public StandardTransactionBuilder threadBound() {
        this.threadBound = true;
        this.singleThreaded = true;
        return this;
    }

    @Override
    public StandardTransactionBuilder readOnly() {
        this.isReadOnly = true;
        return this;
    }

    @Override
    public StandardTransactionBuilder enableBatchLoading() {
        verifyUniqueness = false;
        verifyExternalVertexExistence = false;
        acquireLocks = false;
        return this;
    }

    @Override
    public StandardTransactionBuilder setCacheSize(int size) {
        Preconditions.checkArgument(size >= 0);
        this.vertexCacheSize = size;
        this.indexCacheWeight = size / 2;
        return this;
    }

    @Override
    public StandardTransactionBuilder checkInternalVertexExistence() {
        this.verifyInternalVertexExistence = true;
        return this;
    }

    @Override
    public StandardTransactionBuilder setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public StandardTransactionBuilder setMetricsPrefix(String p) {
        this.metricsPrefix = p;
        return this;
    }

    @Override
    public TransactionBuilder setCustomOption(String k, Object v) {
        storageConfiguration.set(k, v);
        return this;
    }

    @Override
    public TitanTransaction start() {
        TransactionConfiguration immutable = new ImmutableTxCfg(isReadOnly,
                assignIDsImmediately, verifyExternalVertexExistence,
                verifyInternalVertexExistence, acquireLocks, verifyUniqueness,
                propertyPrefetching, singleThreaded, threadBound,
                hasTimestamp(), timestamp, timestampProvider,
                indexCacheWeight, vertexCacheSize, metricsPrefix,
                defaultTypeMaker, new BasicConfiguration(TITAN_NS,
                        storageConfiguration.getConfiguration(),
                        Restriction.NONE));
        return graph.newTransaction(immutable);
    }

    /* ##############################################
                    TransactionConfig
    ############################################## */


    @Override
    public final boolean isReadOnly() {
        return isReadOnly;
    }

    @Override
    public final boolean hasAssignIDsImmediately() {
        return assignIDsImmediately;
    }

    @Override
    public final boolean hasVerifyExternalVertexExistence() {
        return verifyExternalVertexExistence;
    }

    @Override
    public final boolean hasVerifyInternalVertexExistence() {
        return verifyInternalVertexExistence;
    }

    @Override
    public final boolean hasAcquireLocks() {
        return acquireLocks;
    }

    @Override
    public final DefaultTypeMaker getAutoEdgeTypeMaker() {
        return defaultTypeMaker;
    }

    @Override
    public final boolean hasVerifyUniqueness() {
        return verifyUniqueness;
    }

    public boolean hasPropertyPrefetching() {
        return propertyPrefetching;
    }

    @Override
    public final boolean isSingleThreaded() {
        return singleThreaded;
    }

    @Override
    public final boolean isThreadBound() {
        return threadBound;
    }

    @Override
    public final int getVertexCacheSize() {
        return vertexCacheSize;
    }

    @Override
    public final long getIndexCacheWeight() {
        return indexCacheWeight;
    }

    @Override
    public boolean hasTimestamp() {
        return timestamp != null;
    }

    @Override
    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

    @Override
    public String getMetricsPrefix() {
        return metricsPrefix;
    }

    @Override
    public boolean hasMetricsPrefix() {
        return null != metricsPrefix;
    }

    @Override
    public long getTimestamp() {
        Preconditions.checkState(timestamp != null, "A timestamp has not been configured");
        return timestamp;
    }

    @Override
    public <V> V getCustomOption(ConfigOption<V> opt) {
        return getCustomOptions().get(opt);
    }

    @Override
    public Configuration getCustomOptions() {
        return new BasicConfiguration(TITAN_NS,
                storageConfiguration.getConfiguration(), Restriction.NONE);
    }

    private static class ImmutableTxCfg implements TransactionConfiguration {

        private final boolean isReadOnly;
        private final boolean hasAssignIDsImmediately;
        private final boolean hasVerifyExternalVertexExistence;
        private final boolean hasVerifyInternalVertexExistence;
        private final boolean hasAcquireLocks;
        private final boolean hasVerifyUniqueness;
        private final boolean hasPropertyPrefetching;
        private final boolean isSingleThreaded;
        private final boolean isThreadBound;
        private final long indexCacheWeight;
        private final int vertexCacheSize;
        private final DefaultTypeMaker defaultTypeMaker;

        private final TransactionHandleConfig handleConfig;

        public ImmutableTxCfg(boolean isReadOnly,
                boolean hasAssignIDsImmediately,
                boolean hasVerifyExternalVertexExistence,
                boolean hasVerifyInternalVertexExistence,
                boolean hasAcquireLocks, boolean hasVerifyUniqueness,
                boolean hasPropertyPrefetching, boolean isSingleThreaded,
                boolean isThreadBound, boolean hasTimestamp, Long timestamp, TimestampProvider timestampProvider,
                long indexCacheWeight, int vertexCacheSize,
                String metricsPrefix, DefaultTypeMaker defaultTypeMaker,
                Configuration storageConfiguration) {
            this.isReadOnly = isReadOnly;
            this.hasAssignIDsImmediately = hasAssignIDsImmediately;
            this.hasVerifyExternalVertexExistence = hasVerifyExternalVertexExistence;
            this.hasVerifyInternalVertexExistence = hasVerifyInternalVertexExistence;
            this.hasAcquireLocks = hasAcquireLocks;
            this.hasVerifyUniqueness = hasVerifyUniqueness;
            this.hasPropertyPrefetching = hasPropertyPrefetching;
            this.isSingleThreaded = isSingleThreaded;
            this.isThreadBound = isThreadBound;
            this.indexCacheWeight = indexCacheWeight;
            this.vertexCacheSize = vertexCacheSize;
            this.defaultTypeMaker = defaultTypeMaker;
            this.handleConfig = new StandardTransactionConfig.Builder()
                    .timestampProvider(timestampProvider).timestamp(timestamp)
                    .metricsPrefix(metricsPrefix)
                    .customOptions(storageConfiguration).build();
        }

        @Override
        public boolean isReadOnly() {
            return isReadOnly;
        }

        @Override
        public boolean hasAssignIDsImmediately() {
            return hasAssignIDsImmediately;
        }

        @Override
        public boolean hasVerifyExternalVertexExistence() {
            return hasVerifyExternalVertexExistence;
        }

        @Override
        public boolean hasVerifyInternalVertexExistence() {
            return hasVerifyInternalVertexExistence;
        }

        @Override
        public boolean hasAcquireLocks() {
            return hasAcquireLocks;
        }

        @Override
        public DefaultTypeMaker getAutoEdgeTypeMaker() {
            return defaultTypeMaker;
        }

        @Override
        public boolean hasVerifyUniqueness() {
            return hasVerifyUniqueness;
        }

        @Override
        public boolean hasPropertyPrefetching() {
            return hasPropertyPrefetching;
        }

        @Override
        public boolean isSingleThreaded() {
            return isSingleThreaded;
        }

        @Override
        public boolean isThreadBound() {
            return isThreadBound;
        }

        @Override
        public int getVertexCacheSize() {
            return vertexCacheSize;
        }

        @Override
        public long getIndexCacheWeight() {
            return indexCacheWeight;
        }

        @Override
        public boolean hasTimestamp() {
            return handleConfig.hasTimestamp();
        }

        @Override
        public long getTimestamp() {
            return handleConfig.getTimestamp();
        }

        @Override
        public TimestampProvider getTimestampProvider() {
            return handleConfig.getTimestampProvider();
        }

        @Override
        public String getMetricsPrefix() {
            return handleConfig.getMetricsPrefix();
        }

        @Override
        public boolean hasMetricsPrefix() {
            return handleConfig.hasMetricsPrefix();
        }

        @Override
        public <V> V getCustomOption(ConfigOption<V> opt) {
            return handleConfig.getCustomOption(opt);
        }

        @Override
        public Configuration getCustomOptions() {
            return handleConfig.getCustomOptions();
        }
    }
}