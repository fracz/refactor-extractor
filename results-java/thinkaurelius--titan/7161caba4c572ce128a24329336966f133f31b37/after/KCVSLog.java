package com.thinkaurelius.titan.diskstorage.log.kcvs;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.thinkaurelius.titan.core.TitanException;
import com.thinkaurelius.titan.core.attribute.Duration;
import com.thinkaurelius.titan.diskstorage.util.time.*;
import com.thinkaurelius.titan.diskstorage.util.time.StandardDuration;
import com.thinkaurelius.titan.diskstorage.Entry;
import com.thinkaurelius.titan.diskstorage.ReadBuffer;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.configuration.ConfigOption;
import com.thinkaurelius.titan.diskstorage.configuration.Configuration;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.*;
import com.thinkaurelius.titan.diskstorage.log.*;
import com.thinkaurelius.titan.diskstorage.log.util.FutureMessage;
import com.thinkaurelius.titan.diskstorage.log.util.ProcessMessageJob;
import com.thinkaurelius.titan.diskstorage.util.*;

import static com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration.*;

import com.thinkaurelius.titan.graphdb.database.serialize.DataOutput;
import com.thinkaurelius.titan.util.system.BackgroundThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link Log} wrapped around a {@link KeyColumnValueStore}. Each message is written as a column-value pair ({@link Entry})
 * into a timeslice slot. A timeslice slot is uniquely identified by:
 * <ul>
 *     <li>The partition id: On storage backends that are key-ordered, a partition bit width can be configured which configures the number of
 *     first bits that comprise the partition id. On unordered storage backends, this is always 0</li>
 *     <li>A bucket id: The number of parallel buckets that should be maintained is configured by
 *     {@link com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration#LOG_NUM_BUCKETS}. Messages are written to the buckets
 *     in round-robin fashion and each bucket is identified by a bucket id.
 *     Having multiple buckets per timeslice allows for load balancing across multiple keys in the storage backend.</li>
 *     <li>The start time of the timeslice: Each time slice is {@link #TIMESLICE_INTERVAL} microseconds long. And all messages that are added between
 *     start-time and start-time+{@link #TIMESLICE_INTERVAL} end up in the same timeslice. For high throughput logs that might be more messages
 *     than the underlying storage backend can handle per key. In that case, ensure that (2^(partition-bit-width) x (num-bucekts) is large enough
 *     to distribute the load.</li>
 * </ul>
 *
 * Each message is uniquely identified by its timestamp, sender id (which uniquely identifies a particular instance of {@link KCVSLogManager}), and the
 * message id (which is auto-incrementing). These three data points comprise the column of a log message. The actual content of the message
 * is written into the value.
 * </p>
 * When {@link MessageReader} are registered, one reader thread per partition id and bucket is created which periodically (as configured) checks for
 * new messages in the storage backend and invokes the reader. </br>
 * Read-markers are maintained (for each partition-id & bucket id combination) under a dedicated key in the same {@link KeyColumnValueStoreManager} as the
 * log messages. The read markers are updated to the current position before each new iteration of reading messages from the log. If the system fails
 * while reading a batch of messages, a subsequently restarted log reader may therefore read messages twice. Hence, {@link MessageReader} implementations
 * should exhibit correct behavior for the (rare) circumstance that messages are read twice.
 *
 * Note: All time values in this class are in mircoseconds. Hence, there are many cases where milliseconds are converted to microseconds.
 *
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class KCVSLog implements Log, BackendOperation.TransactionalProvider {

    private static final Logger log = LoggerFactory.getLogger(KCVSLog.class);

    //########## Configuration Options #############

    public static final ConfigOption<Duration> LOG_MAX_WRITE_TIME = new ConfigOption<Duration>(LOG_NS,"max-write-time",
            "Maximum time in ms to try persisting log messages against the backend before failing.",
            ConfigOption.Type.MASKABLE, new StandardDuration(10000L, TimeUnit.MILLISECONDS));

    public static final ConfigOption<Duration> LOG_MAX_READ_TIME = new ConfigOption<Duration>(LOG_NS,"max-read-time",
            "Maximum time in ms to try reading log messages from the backend before failing.",
            ConfigOption.Type.MASKABLE, new StandardDuration(4000L, TimeUnit.MILLISECONDS));

    public static final ConfigOption<Duration> LOG_READ_LAG_TIME = new ConfigOption<Duration>(LOG_NS,"read-lag-time",
            "Maximum time in ms that it may take for reads to appear in the backend. If a write does not become" +
                    "visible in the storage backend in this amount of time, a log reader might miss the message.",
            ConfigOption.Type.MASKABLE, new StandardDuration(500L, TimeUnit.MILLISECONDS));

    public static final ConfigOption<Boolean> LOG_KEY_CONSISTENT = new ConfigOption<Boolean>(LOG_NS,"key-consistent",
            "Whether to require consistency for log reading and writing messages to the storage backend",
            ConfigOption.Type.MASKABLE, false);

    //########## INTERNAL CONSTANTS #############

    /**
     * The time period that is stored under one key in the underlying KCVS.
     * This value should NEVER be changed since this will cause backwards incompatibility.
     * This setting is not configurable. If too many messages end up under one key, please
     * configure either 1) the number of buckets or 2) introduce partitioning.
     */
    public static final long TIMESLICE_INTERVAL = 100L * 1000 * 1000 ; //100 seconds

    /**
     * For batch sending to make sense against a KCVS, the maximum send/delivery delay must be at least
     * this number. If the delivery delay is configured to be smaller than this time interval, messages
     * will be send immediately since batching will likely be ineffective.
     */
    private final static Duration MIN_DELIVERY_DELAY = new StandardDuration(10L, TimeUnit.MILLISECONDS);

    /**
     * Multiplier for the maximum number of messages to hold in the outgoing message queue before producing back pressure.
     * Multiplied with the message sending batch size.
     * If back pressure is a regular occurrence, decrease the sending interval or increase the sending batch size
     */
    private final static int BATCH_SIZE_MULTIPLIER = 10;
    /**
     * Wait time after close() is called for all ongoing jobs to finish and shut down.
     */
    private final static Duration CLOSE_DOWN_WAIT = new StandardDuration(10L, TimeUnit.SECONDS);
    /**
     * Time before a registered reader starts processing messages
     */
    private final static Duration INITIAL_READER_DELAY = new StandardDuration(100L, TimeUnit.MILLISECONDS);

    private final static Duration FOREVER = new StandardDuration(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // TODO remove this

    //########## INTERNAL SETTING MANAGEMENT #############

    /**
     * All system settings use this value for the partition id (first 4 byte of the key).
     * This is all 1s in binary representation which would be an illegal value for a partition id. Hence,
     * we avoid conflict.
     */
    private final static int SYSTEM_PARTITION_ID = 0xFFFFFFFF;

    /**
     * The first byte of any system column is used to indicate the type of column. The next two variables define
     * the prefixes for message counter columns (i.e. keeping of the log message numbers) and for the marker columns
     * (i.e. keeping track of the timestamps to which it has been read)
     */
    private final static byte MESSAGE_COUNTER = 1;
    private final static byte MARKER_PREFIX = 2;
    /**
     * Since the message counter column is nothing but the prefix, we can define it statically up front
     */
    private final static StaticBuffer MESSAGE_COUNTER_COLUMN = new WriteByteBuffer(1).putByte(MESSAGE_COUNTER).getStaticBuffer();

    private static final Random random = new Random();

    /**
     * Associated {@link LogManager}
     */
    private final KCVSLogManager manager;
    /**
     * Name of the log - logs are uniquely identified by name
     */
    private final String name;
    /**
     * The KCVSStore wrapped by this log
     */
    private final KeyColumnValueStore store;
    /**
     * The read marker which indicates where to start reading from the log
     */
    private final ReadMarker readMarker;

    /**
     * The number of buckets into which each time slice is subdivided. Increasing the number of buckets load balances
     * the reads and writes to the log.
     */
    private final int numBuckets;
    private final boolean keyConsistentOperations;

    private final int sendBatchSize;
    private final Duration maxSendDelay;
    private final Duration maxWriteTime;
    /**
     * Used for batch addition of messages to the log. Newly added entries are buffered in this queue before being written in batch
     */
    private final ArrayBlockingQueue<MessageEnvelope> outgoingMsg;
    /**
     * Background thread which periodically writes out the queued up messages. TODO: consider batching messages across ALL logs
     */
    private final SendThread sendThread;

    private final int numReadThreads;
    private final int maxReadMsg;
    private final Duration readPollingInterval;
    private final Duration readLagTime;
    private final Duration maxReadTime;
    private final boolean allowReadMarkerRecovery = true;

    /**
     * Thread pool to read messages in the specified interval from the various keys in a time slice AND to process
     * messages. So, both reading and processing messages is done in the same thread pool.
     */
    private ScheduledExecutorService readExecutor;
    /**
     * Individual jobs that pull messages from the keys that comprise one time slice
     */
    private MessagePuller[] msgPullers;

    /**
     * Counter used to write messages to different buckets in a round-robin fashion
     */
    private final AtomicLong numBucketCounter;
    /**
     * Counter for the message ids of this sender
     */
    private final AtomicLong numMsgCounter;
    /**
     * Registered readers for this log
     */
    private final List<MessageReader> readers;
    /**
     * Whether this log is open (i.e. accepts writes)
     */
    private volatile boolean isOpen;
    /**
     * Source of timestamps since UNIX Epoch; also provides our time resolution (e.g. microseconds)
     */
    private final TimestampProvider times;

    public KCVSLog(String name, KCVSLogManager manager, KeyColumnValueStore store, ReadMarker readMarker, Configuration config) {
        Preconditions.checkArgument(manager != null && name != null && readMarker != null && store != null && config!=null);
        this.name=name;
        this.manager=manager;
        this.store=store;
        this.readMarker=readMarker;

        this.times = config.get(TIMESTAMP_PROVIDER);
        this.keyConsistentOperations = config.get(LOG_KEY_CONSISTENT);
        this.numBuckets = config.get(LOG_NUM_BUCKETS);
        Preconditions.checkArgument(numBuckets>=1 && numBuckets<=Integer.MAX_VALUE);

        sendBatchSize = config.get(LOG_SEND_BATCH_SIZE);
        maxSendDelay = config.get(LOG_SEND_DELAY);
        maxWriteTime = config.get(LOG_MAX_WRITE_TIME);

        numReadThreads = config.get(LOG_READ_THREADS);
        maxReadMsg = config.get(LOG_READ_BATCH_SIZE);
        readPollingInterval = config.get(LOG_READ_INTERVAL);
        readLagTime = config.get(LOG_READ_LAG_TIME).add(maxSendDelay);
        maxReadTime = config.get(LOG_MAX_READ_TIME);

        if (MIN_DELIVERY_DELAY.compareTo(maxSendDelay) <= 0) { // No need to locally queue messages since they will be sent immediately
            outgoingMsg = new ArrayBlockingQueue<MessageEnvelope>(sendBatchSize*BATCH_SIZE_MULTIPLIER);
            sendThread = new SendThread();
            sendThread.start();
        } else {
            outgoingMsg = null;
            sendThread = null;
        }

        //These will be initialized when the first readers are registered (see below)
        readExecutor = null;
        msgPullers = null;

        this.numMsgCounter = new AtomicLong(readSetting(manager.senderId, MESSAGE_COUNTER_COLUMN, 0));
        this.numBucketCounter = new AtomicLong(0);
        this.readers = new ArrayList<MessageReader>();
        this.isOpen = true;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Closes the log by terminating all threads and waiting for their termination.
     *
     * @throws StorageException
     */
    @Override
    public synchronized void close() throws StorageException {
        this.isOpen = false;
        if (readExecutor!=null) readExecutor.shutdown();
        if (sendThread!=null) sendThread.close(CLOSE_DOWN_WAIT.getLength(TimeUnit.MICROSECONDS),TimeUnit.MICROSECONDS);
        if (readExecutor!=null) {
            try {
                readExecutor.awaitTermination(1,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Could not terminate reader thread pool for KCVSLog "+name+" due to interruption");
            }
            if (!readExecutor.isTerminated()) {
                readExecutor.shutdownNow();
                log.error("Reader thread pool for KCVSLog "+name+" did not shut down in time - could not clean up or set read markers");
            } else {
                for (MessagePuller puller : msgPullers) {
                    puller.close();
                }
            }
        }
        writeSetting(manager.senderId, MESSAGE_COUNTER_COLUMN, numMsgCounter.get());
        store.close();
        manager.closedLog(this);
    }

    @Override
    public StoreTransaction openTx() throws StorageException {
        StandardTransactionHandleConfig config;
        if (keyConsistentOperations) {
            config = StandardTransactionHandleConfig.of(times,manager.storeManager.getFeatures().getKeyConsistentTxConfig());
        } else {
            config = StandardTransactionHandleConfig.of(times);
        }
        return manager.storeManager.beginTransaction(config);
    }

    /**
     * ###################################
     *  Message Serialization & Utility
     * ###################################
     */

    private int getTimeSlice(Timepoint timestamp) {
        long value = timestamp.getTimestamp(times.getUnit()) / TIMESLICE_INTERVAL;
        if (value>Integer.MAX_VALUE || value<0) throw new IllegalArgumentException("Timestamp overflow detected: " + timestamp);
        return (int)value;
    }

    private StaticBuffer getLogKey(final int partitionId, final int bucketId, final int timeslice) {
        Preconditions.checkArgument(partitionId>=0 && partitionId<(1<<manager.partitionBitWidth));
        Preconditions.checkArgument(bucketId>=0 && bucketId<numBuckets);
        DataOutput o = manager.serializer.getDataOutput(3 * 4);
        o.putInt((partitionId<<(32-manager.partitionBitWidth))); //Offset to put significant bits in front
        o.putInt(bucketId);
        o.putInt(timeslice);
        return o.getStaticBuffer();
    }

    private Entry writeMessage(KCVSMessage msg) {
        StaticBuffer content = msg.getContent();
        DataOutput out = manager.serializer.getDataOutput(8 + 8 + manager.senderId.length() + 2 + content.length());
        final long rawTimestamp = msg.getTimestamp(times.getUnit());
        Preconditions.checkArgument(rawTimestamp>0);
        out.putLong(rawTimestamp);
        out.writeObjectNotNull(manager.senderId);
        out.putLong(numMsgCounter.incrementAndGet());
        final int valuePos = out.getPosition();
        out.putBytes(content);
        return new StaticArrayEntry(out.getStaticBuffer(),valuePos);
    }

    private KCVSMessage parseMessage(Entry msg) {
        ReadBuffer r = msg.asReadBuffer();
        Timepoint timestamp = new StandardTimepoint(r.getLong(), times);
        String senderId = manager.serializer.readObjectNotNull(r,String.class);
        return new KCVSMessage(msg.getValue(),timestamp,senderId);
    }

    /**
     * ###################################
     *  Message Sending
     * ###################################
     */

    @Override
    public Future<Message> add(StaticBuffer content) {
        return add(content,manager.defaultWritePartitionIds[random.nextInt(manager.defaultWritePartitionIds.length)]);
    }

    @Override
    public Future<Message> add(StaticBuffer content, StaticBuffer key) {
        Preconditions.checkArgument(key!=null && key.length()>0,"Invalid key provided: %s",key);
        int partitionId = 0;
        //Get first 4 byte if exist in key...
        for (int i=0;i<4;i++) {
            int b;
            if (key.length()>i) b = key.getByte(i) & 0xFF;
            else b = 0;
            partitionId = (partitionId<<8) + b;
        }
        assert manager.partitionBitWidth>=0 && manager.partitionBitWidth<=32;
        //and then extract the number of partitions bits
        if (manager.partitionBitWidth==0) partitionId=0;
        else partitionId = partitionId>>>(32-manager.partitionBitWidth);
        return add(content, partitionId);
    }

    /**
     * Adds the given message (content) to the timeslice for the partition identified by the provided partitionId.
     *
     * @param content
     * @param partitionId
     * @return
     */
    private Future<Message> add(StaticBuffer content, int partitionId) {
        Preconditions.checkArgument(isOpen,"Log {} has been closed",name);
        Preconditions.checkArgument(content!=null && content.length()>0,"Content is empty");
        Preconditions.checkArgument(partitionId>=0 && partitionId<(1<<manager.partitionBitWidth),"Invalid partition id: %s",partitionId);
        final Timepoint timestamp = times.getTime();
        KCVSMessage msg = new KCVSMessage(content,timestamp,manager.senderId);
        FutureMessage fmsg = new FutureMessage(msg);

        StaticBuffer key=getLogKey(partitionId,(int)(numBucketCounter.incrementAndGet()%numBuckets),getTimeSlice(timestamp));
        MessageEnvelope envelope = new MessageEnvelope(fmsg,key,writeMessage(msg));

        if (outgoingMsg==null) {
            sendMessages(ImmutableList.of(envelope));
        } else {
            try {
                outgoingMsg.put(envelope); //Produces back pressure when full
            } catch (InterruptedException e) {
                throw new TitanException("Got interrupted waiting to send message",e);
            }
        }
        return fmsg;
    }

    /**
     * Helper class to hold the message and its serialization for writing
     */
    private static class MessageEnvelope {

        final FutureMessage<KCVSMessage> message;
        final StaticBuffer key;
        final Entry entry;

        private MessageEnvelope(FutureMessage<KCVSMessage> message, StaticBuffer key, Entry entry) {
            this.message = message;
            this.key = key;
            this.entry = entry;
        }
    }

    /**
     * Sends a batch of messages by persisting them to the storage backend.
     *
     * @param msgEnvelopes
     */
    private void sendMessages(final List<MessageEnvelope> msgEnvelopes) {
        try {
            boolean success=BackendOperation.execute(new BackendOperation.Transactional<Boolean>() {
                @Override
                public Boolean call(StoreTransaction txh) throws StorageException {
                    ListMultimap<StaticBuffer,Entry> mutations = ArrayListMultimap.create();
                    for (MessageEnvelope env : msgEnvelopes) {
                        mutations.put(env.key,env.entry);
                    }

                    Map<StaticBuffer,KCVMutation> muts = new HashMap<StaticBuffer, KCVMutation>(mutations.keySet().size());
                    for (StaticBuffer key : mutations.keySet()) {
                        muts.put(key,new KCVMutation(mutations.get(key),KeyColumnValueStore.NO_DELETIONS));
                    }
                    manager.storeManager.mutateMany(ImmutableMap.of(store.getName(),muts),txh);
                    return Boolean.TRUE;
                }
                @Override
                public String toString() {
                    return "messageSending";
                }
            },this, times, maxWriteTime);
            Preconditions.checkState(success);
            log.debug("Wrote {} messages to backend",msgEnvelopes.size());
            for (MessageEnvelope msgEnvelope : msgEnvelopes)
                msgEnvelope.message.delivered();
        } catch (TitanException e) {
            for (MessageEnvelope msgEnvelope : msgEnvelopes)
                msgEnvelope.message.failed(e);
            throw e;
        }
    }

    /**
     * This background thread only gets started when messages are locally queued for up to a maximum number of microseconds
     * or until the maximum number of local messages is reached.
     * This thread waits for either event and then triggers {@link #sendMessages(java.util.List)} call to persist the messages.
     */
    private class SendThread extends BackgroundThread {

        private List<MessageEnvelope> toSend;

        public SendThread() {
            super("KCVSLogSend"+name, false);
            toSend = new ArrayList<MessageEnvelope>(sendBatchSize*3/2);
        }

        private Duration timeSinceFirstMsg() {

            Duration sinceFirst =  ZeroDuration.INSTANCE;

            if (!toSend.isEmpty()) {
                Timepoint firstTimestamp = toSend.get(0).message.getMessage().getTimestampMicro();
                Timepoint nowTimestamp   = times.getTime();

                if (firstTimestamp.compareTo(nowTimestamp) < 0) {
                    long firstRaw = firstTimestamp.getTimestamp(times.getUnit());
                    long nowRaw = nowTimestamp.getTimestamp(times.getUnit());
                    assert firstRaw < nowRaw;
                    sinceFirst = new StandardDuration(nowRaw - firstRaw, times.getUnit());
                }
            }

            return sinceFirst;
        }

        private Duration maxWaitTime() {
            if (!toSend.isEmpty()) {
                return maxSendDelay.sub(timeSinceFirstMsg());
            }

            return FOREVER;
        }

        @Override
        protected void waitCondition() throws InterruptedException {
            final TimeUnit schedulingTimeUnit = TimeUnit.MICROSECONDS;
            MessageEnvelope msg = outgoingMsg.poll(maxWaitTime().getLength(schedulingTimeUnit), schedulingTimeUnit);
            if (msg!=null) toSend.add(msg);
        }

        @Override
        protected void action() {
            MessageEnvelope msg;
            //Opportunistically drain the queue for up to the batch-send-size number of messages before evaluating condition
            while (toSend.size()<sendBatchSize && (msg=outgoingMsg.poll())!=null) {
                toSend.add(msg);
            }
            //Evaluate send condition: 1) Is the oldest message waiting longer than the delay? or 2) Do we have enough messages to send?
            if (!toSend.isEmpty() && (maxSendDelay.compareTo(timeSinceFirstMsg()) <= 0 || toSend.size() >= sendBatchSize)) {
                try {
                    sendMessages(toSend);
                } finally {
                    toSend.clear();
                }
            }
        }

        @Override
        protected void cleanup() {
            //Send all remaining messages
            if (!toSend.isEmpty() || !outgoingMsg.isEmpty()) {
                //There are still messages waiting to be sent
                toSend.addAll(outgoingMsg);
                for (int i=0;i<toSend.size();i=i+sendBatchSize) {
                    List<MessageEnvelope> subset = toSend.subList(i,Math.min(toSend.size(),i+sendBatchSize));
                    try {
                        sendMessages(subset);
                    } catch (RuntimeException e) {
                        //Fail all remaining messages
                        for (int j=i+sendBatchSize;j<toSend.size();j++) {
                            toSend.get(j).message.failed(e);
                        }
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * ###################################
     *  Message Reading
     * ###################################
     */

    @Override
    public synchronized void registerReader(MessageReader... reader) {
        Preconditions.checkArgument(isOpen,"Log {} has been closed",name);
        Preconditions.checkArgument(reader!=null && reader.length>0,"Must specify at least one reader");
        registerReaders(Arrays.asList(reader));
    }

    @Override
    public synchronized void registerReaders(Iterable<MessageReader> readers) {
        Preconditions.checkArgument(isOpen,"Log {} has been closed",name);
        Preconditions.checkArgument(!Iterables.isEmpty(readers),"Must specify at least one reader");
        boolean firstRegistration = this.readers.isEmpty();
        for (MessageReader reader : readers) {
            Preconditions.checkNotNull(reader);
            if (!this.readers.contains(reader)) this.readers.add(reader);
        }
        if (firstRegistration && !this.readers.isEmpty()) {
            readExecutor = new ScheduledThreadPoolExecutor(numReadThreads,new RejectedExecutionHandler() {
            //Custom rejection handler so that messages are processed in-thread when executor has been closed
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    r.run();
                }
            });
            msgPullers = new MessagePuller[manager.readPartitionIds.length*numBuckets];
            int pos = 0;
            for (int partitionId : manager.readPartitionIds) {
                for (int bucketId = 0; bucketId < numBuckets; bucketId++) {
                    msgPullers[pos]=new MessagePuller(partitionId,bucketId);
                    final TimeUnit schedulingTimeUnit = TimeUnit.MICROSECONDS;
                    log.debug("Creating log read executor: initialDelay={} delay={} unit={}", INITIAL_READER_DELAY.getLength(schedulingTimeUnit), readPollingInterval.getLength(schedulingTimeUnit), schedulingTimeUnit);
                    readExecutor.scheduleWithFixedDelay(
                            msgPullers[pos],
                            INITIAL_READER_DELAY.getLength(schedulingTimeUnit),
                            readPollingInterval.getLength(schedulingTimeUnit),
                            schedulingTimeUnit);
                    pos++;
                }
            }
        }
    }

    @Override
    public synchronized boolean unregisterReader(MessageReader reader) {
        Preconditions.checkArgument(isOpen,"Log {} has been closed",name);
        return this.readers.remove(reader);
    }

    /**
     * Thread which runs to read all messages from a particular partition id and bucket up to the next timeslice
     * or current timestamp minus the configured read lag time {@link #LOG_READ_LAG_TIME}.
     * The read marker is used to initialize the start time to read from. If a read marker is configured, then
     * the read marker time is looked up for initialization.
     */
    private class MessagePuller implements Runnable {

        private final int bucketId;
        private final int partitionId;

        private Timepoint nextTimepoint;
        private long nextTimestamp;

        private MessagePuller(final int partitionId, final int bucketId) {
            this.bucketId = bucketId;
            this.partitionId = partitionId;
            if (!readMarker.hasIdentifier()) {
                this.nextTimepoint = readMarker.getStartTime(times);
            } else {
                long savedTimestamp = readSetting(readMarker.getIdentifier(),getMarkerColumn(partitionId,bucketId),readMarker.getStartTime(times).getNativeTimestamp());
                this.nextTimepoint = new StandardTimepoint(savedTimestamp, times);
            }
            this.nextTimestamp = nextTimepoint.getTimestamp(times.getUnit());
        }

        @Override
        public void run() {
            if (allowReadMarkerRecovery) setReadMarker();
            try {
                final int timeslice = getTimeSlice(nextTimepoint);
                long tdelta = times.getTime().sub(readLagTime).getTimestamp(times.getUnit());
                long maxTime = Math.min(tdelta, (timeslice + 1) * TIMESLICE_INTERVAL);
                // maxTime must be at least nextTimestamp, or else we will have a slice start after slice end
                maxTime = Math.max(maxTime, nextTimestamp);
                StaticBuffer logKey = getLogKey(partitionId,bucketId,timeslice);
                KeySliceQuery query = new KeySliceQuery(logKey, BufferUtil.getLongBuffer(nextTimestamp), BufferUtil.getLongBuffer(maxTime));
                query.setLimit(maxReadMsg);
                List<Entry> entries= BackendOperation.execute(getOperation(query),KCVSLog.this,times,maxReadTime);
                prepareMessageProcessing(entries);
                if (entries.size()>=maxReadMsg) {
                    /*Read another set of messages to ensure that we have exhausted all messages to the next timestamp.
                    Since we have reached the request limit, it may be possible that there are additional messages
                    with the same timestamp which we would miss on subsequent iterations */
                    Entry lastEntry = entries.get(entries.size()-1);
                    maxTime = lastEntry.getLong(0)+2; //Adding 2 microseconds (=> very few extra messages), not adding one to avoid that the slice is possibly empty
                    //Retrieve all messages up to this adjusted timepoint (no limit this time => get all entries to that point)
                    query = new KeySliceQuery(logKey, BufferUtil.nextBiggerBuffer(lastEntry.getColumn()), BufferUtil.getLongBuffer(maxTime));
                    List<Entry> extraEntries = BackendOperation.execute(getOperation(query),KCVSLog.this,times,maxReadTime);
                    prepareMessageProcessing(extraEntries);
                }
                nextTimepoint = new StandardTimepoint(maxTime, times);
                nextTimestamp = maxTime;
            } catch (Throwable e) {
                log.error("Could not read messages for timestamp ["+nextTimepoint+"] - will attempt again",e);
            }
        }

        private void prepareMessageProcessing(List<Entry> entries) {
            for (Entry entry : entries) {
                KCVSMessage message = parseMessage(entry);
                for (MessageReader reader : readers) {
                    readExecutor.submit(new ProcessMessageJob(message,reader));
                }
            }
        }

        private void setReadMarker() {
            if (readMarker.hasIdentifier()) {
                try {
                    writeSetting(readMarker.getIdentifier(), getMarkerColumn(partitionId, bucketId), nextTimestamp);
                } catch (Throwable e) {
                    log.error("Could not persist read marker [" + readMarker.getIdentifier() + "] on bucket ["+bucketId+"] + partition ["+partitionId+"]",e);
                }
            }
        }

        private void close() {
            setReadMarker();
        }

        private BackendOperation.Transactional<List<Entry>> getOperation(final KeySliceQuery query) {
            return new BackendOperation.Transactional<List<Entry>>() {
                @Override
                public List<Entry> call(StoreTransaction txh) throws StorageException {
                    return store.getSlice(query,txh);
                }
                @Override
                public String toString() {
                    return "messageReading@"+partitionId+":"+bucketId;
                }
            };
        }

    }

    /**
     * ###################################
     *  Getting/setting Log Settings
     * ###################################
     */

    private StaticBuffer getMarkerColumn(int partitionId, int bucketId) {
        DataOutput out = manager.serializer.getDataOutput(1+ 4 + 4);
        out.putByte(MARKER_PREFIX);
        out.putInt(partitionId);
        out.putInt(bucketId);
        return out.getStaticBuffer();
    }


    private StaticBuffer getSettingKey(String identifier) {
        DataOutput out = manager.serializer.getDataOutput(4 + 2 + identifier.length());
        out.putInt(SYSTEM_PARTITION_ID);
        out.writeObjectNotNull(identifier);
        return out.getStaticBuffer();
    }

    private long readSetting(String identifier, final StaticBuffer column, long defaultValue) {
        final StaticBuffer key = getSettingKey(identifier);
        StaticBuffer value = BackendOperation.execute(new BackendOperation.Transactional<StaticBuffer>() {
            @Override
            public StaticBuffer call(StoreTransaction txh) throws StorageException {
                return KCVSUtil.get(store,key,column,txh);
            }
            @Override
            public String toString() {
                return "readingLogSetting";
            }
        },this,times,maxReadTime);
        if (value==null) return defaultValue;
        else {
            Preconditions.checkArgument(value.length()==8);
            return value.getLong(0);
        }
    }

    private void writeSetting(String identifier, final StaticBuffer column, long value) {
        final StaticBuffer key = getSettingKey(identifier);
        final Entry add = StaticArrayEntry.of(column, BufferUtil.getLongBuffer(value));
        Boolean status = BackendOperation.execute(new BackendOperation.Transactional<Boolean>() {
            @Override
            public Boolean call(StoreTransaction txh) throws StorageException {
                store.mutate(key,ImmutableList.of(add),KeyColumnValueStore.NO_DELETIONS,txh);
                return Boolean.TRUE;
            }
            @Override
            public String toString() {
                return "writingLogSetting";
            }
        },this, times, maxWriteTime);
        Preconditions.checkState(status);
    }


}