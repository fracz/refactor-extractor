 /**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cassandra.dht;

 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Condition;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.io.File;
 import java.net.InetAddress;

 import org.apache.log4j.Logger;

 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;

 import org.apache.cassandra.locator.TokenMetadata;
 import org.apache.cassandra.net.*;
 import org.apache.cassandra.net.io.StreamContextManager;
 import org.apache.cassandra.net.io.IStreamComplete;
 import org.apache.cassandra.service.StorageService;
 import org.apache.cassandra.service.StorageLoadBalancer;
 import org.apache.cassandra.service.StreamManager;
 import org.apache.cassandra.utils.LogUtil;
 import org.apache.cassandra.utils.SimpleCondition;
 import org.apache.cassandra.utils.FBUtilities;
 import org.apache.cassandra.config.DatabaseDescriptor;
 import org.apache.cassandra.gms.Gossiper;
 import org.apache.cassandra.gms.ApplicationState;
 import org.apache.cassandra.io.DataInputBuffer;
 import org.apache.cassandra.io.SSTableReader;
 import org.apache.cassandra.io.SSTableWriter;
 import org.apache.cassandra.db.ColumnFamilyStore;
 import org.apache.cassandra.db.Table;


 /**
  * This class handles the bootstrapping responsibilities for the local endpoint.
  *
  *  - bootstrapTokenVerb asks the most-loaded node what Token to use to split its Range in two.
  *  - bootstrapMetadataVerb tells source nodes to send us the necessary Ranges
  *  - source nodes send bootStrapInitiateVerb to us to say "get ready to receive data" [if there is data to send]
  *  - when we have everything set up to receive the data, we send bootStrapInitiateDoneVerb back to the source nodes and they start streaming
  *  - when streaming is complete, we send bootStrapTerminateVerb to the source so it can clean up on its end
  */
public class BootStrapper
{
    public static final long INITIAL_DELAY = 30 * 1000; //ms

    static final Logger logger = Logger.getLogger(BootStrapper.class);

    /* endpoints that need to be bootstrapped */
    protected final List<InetAddress> targets;
    /* tokens of the nodes being bootstrapped. */
    protected final Token[] tokens;
    protected final TokenMetadata tokenMetadata;

    public BootStrapper(List<InetAddress> targets, Token... token)
    {
        this.targets = targets;
        tokens = token;
        tokenMetadata = StorageService.instance().getTokenMetadata();
    }

    Map<Range, List<BootstrapSourceTarget>> getRangesWithSourceTarget()
    {
        /* copy the token to endpoint map */
        Map<Token, InetAddress> tokenToEndPointMap = tokenMetadata.cloneTokenEndPointMap();
        /* remove the tokens associated with the endpoints being bootstrapped */
        for (Token token : tokens)
        {
            tokenToEndPointMap.remove(token);
        }

        Set<Token> oldTokens = new HashSet<Token>( tokenToEndPointMap.keySet() );
        Range[] oldRanges = StorageService.instance().getAllRanges(oldTokens);
        if (logger.isDebugEnabled())
          logger.debug("Total number of old ranges " + oldRanges.length);
        /*
         * Find the ranges that are split. Maintain a mapping between
         * the range being split and the list of subranges.
        */
        Map<Range, List<Range>> splitRanges = LeaveJoinProtocolHelper.getRangeSplitRangeMapping(oldRanges, tokens);
        /* Calculate the list of nodes that handle the old ranges */
        Map<Range, List<InetAddress>> oldRangeToEndPointMap = StorageService.instance().constructRangeToEndPointMap(oldRanges, tokenToEndPointMap);
        /* Mapping of split ranges to the list of endpoints responsible for the range */
        Map<Range, List<InetAddress>> replicasForSplitRanges = new HashMap<Range, List<InetAddress>>();
        Set<Range> rangesSplit = splitRanges.keySet();
        for ( Range splitRange : rangesSplit )
        {
            replicasForSplitRanges.put( splitRange, oldRangeToEndPointMap.get(splitRange) );
        }
        /* Remove the ranges that are split. */
        for ( Range splitRange : rangesSplit )
        {
            oldRangeToEndPointMap.remove(splitRange);
        }

        /* Add the subranges of the split range to the map with the same replica set. */
        for ( Range splitRange : rangesSplit )
        {
            List<Range> subRanges = splitRanges.get(splitRange);
            List<InetAddress> replicas = replicasForSplitRanges.get(splitRange);
            for ( Range subRange : subRanges )
            {
                /* Make sure we clone or else we are hammered. */
                oldRangeToEndPointMap.put(subRange, new ArrayList<InetAddress>(replicas));
            }
        }

        /* Add the new token and re-calculate the range assignments */
        Collections.addAll( oldTokens, tokens);
        Range[] newRanges = StorageService.instance().getAllRanges(oldTokens);

        if (logger.isDebugEnabled())
          logger.debug("Total number of new ranges " + newRanges.length);
        /* Calculate the list of nodes that handle the new ranges */
        Map<Range, List<InetAddress>> newRangeToEndPointMap = StorageService.instance().constructRangeToEndPointMap(newRanges);
        /* Calculate ranges that need to be sent and from whom to where */
        Map<Range, List<BootstrapSourceTarget>> rangesWithSourceTarget = LeaveJoinProtocolHelper.getRangeSourceTargetInfo(oldRangeToEndPointMap, newRangeToEndPointMap);
        return rangesWithSourceTarget;
    }

    private static Token<?> getBootstrapTokenFrom(InetAddress maxEndpoint)
    {
        Message message = new Message(FBUtilities.getLocalAddress(), "", StorageService.bootstrapTokenVerbHandler_, ArrayUtils.EMPTY_BYTE_ARRAY);
        BootstrapTokenCallback btc = new BootstrapTokenCallback();
        MessagingService.instance().sendRR(message, maxEndpoint, btc);
        return btc.getToken();
    }

    public void startBootstrap() throws IOException
    {
        logger.info("Starting in bootstrap mode (first, sleeping to get load information)");

        StorageService ss = StorageService.instance();
        StorageLoadBalancer slb = StorageLoadBalancer.instance();

        slb.waitForLoadInfo();

        // if initialtoken was specified, use that.  otherwise, pick a token to assume half the load of the most-loaded node.
        if (DatabaseDescriptor.getInitialToken() == null)
        {
            double maxLoad = 0;
            InetAddress maxEndpoint = null;
            for (Map.Entry<InetAddress,Double> entry : slb.getLoadInfo().entrySet())
            {
                if (maxEndpoint == null || entry.getValue() > maxLoad)
                {
                    maxEndpoint = entry.getKey();
                    maxLoad = entry.getValue();
                }
            }
            if (maxEndpoint == null)
            {
                throw new RuntimeException("No bootstrap sources found");
            }

            if (!maxEndpoint.equals(FBUtilities.getLocalAddress()))
            {
                Token<?> t = getBootstrapTokenFrom(maxEndpoint);
                logger.info("Setting token to " + t + " to assume load from " + maxEndpoint);
                ss.setToken(t);
            }
        }

        new Thread(new Runnable()
        {
            public void run()
            {
                // Mark as not bootstrapping to calculate ranges correctly
                for (int i=0; i< targets.size(); i++)
                {
                    tokenMetadata.setBootstrapping(targets.get(i), false);
                }

                Map<Range, List<BootstrapSourceTarget>> rangesWithSourceTarget = getRangesWithSourceTarget();
                if (logger.isDebugEnabled())
                        logger.debug("Beginning bootstrap process for [" + StringUtils.join(targets, ", ") + "] ...");
                /* Send messages to respective folks to stream data over to the new nodes being bootstrapped */
                try
                {
                    LeaveJoinProtocolHelper.assignWork(rangesWithSourceTarget);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        Gossiper.instance().addApplicationState(StorageService.MODE, new ApplicationState(StorageService.MODE_MOVING));
    }

    public static class BootstrapTokenVerbHandler implements IVerbHandler
    {
        public void doVerb(Message message)
        {
            StorageService ss = StorageService.instance();
            List<String> tokens = ss.getSplits(2);
            assert tokens.size() == 3 : tokens.size();
            Message response;
            try
            {
                response = message.getReply(FBUtilities.getLocalAddress(), tokens.get(1).getBytes("UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new AssertionError();
            }
            MessagingService.instance().sendOneWay(response, message.getFrom());
        }
    }

    public static class BootstrapInitiateDoneVerbHandler implements IVerbHandler
    {
        public void doVerb(Message message)
        {
            if (logger.isDebugEnabled())
              logger.debug("Received a bootstrap initiate done message ...");
            /* Let the Stream Manager do his thing. */
            StreamManager.instance(message.getFrom()).start();
        }
    }

    private static class BootstrapTokenCallback implements IAsyncCallback
    {
        private volatile Token<?> token;
        private final Condition condition = new SimpleCondition();

        public Token<?> getToken()
        {
            try
            {
                condition.await();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            return token;
        }

        public void response(Message msg)
        {
            try
            {
                token = StorageService.getPartitioner().getTokenFactory().fromString(new String(msg.getMessageBody(), "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new AssertionError();
            }
            condition.signalAll();
        }
    }

    public static class BootStrapInitiateVerbHandler implements IVerbHandler
    {
        /*
         * Here we handle the BootstrapInitiateMessage. Here we get the
         * array of StreamContexts. We get file names for the column
         * families associated with the files and replace them with the
         * file names as obtained from the column family store on the
         * receiving end.
        */
        public void doVerb(Message message)
        {
            byte[] body = message.getMessageBody();
            DataInputBuffer bufIn = new DataInputBuffer();
            bufIn.reset(body, body.length);

            try
            {
                BootstrapInitiateMessage biMsg = BootstrapInitiateMessage.serializer().deserialize(bufIn);
                StreamContextManager.StreamContext[] streamContexts = biMsg.getStreamContext();

                Map<String, String> fileNames = getNewNames(streamContexts);
                /*
                 * For each of stream context's in the incoming message
                 * generate the new file names and store the new file names
                 * in the StreamContextManager.
                */
                for (StreamContextManager.StreamContext streamContext : streamContexts )
                {
                    StreamContextManager.StreamStatus streamStatus = new StreamContextManager.StreamStatus(streamContext.getTargetFile(), streamContext.getExpectedBytes() );
                    String file = getNewFileNameFromOldContextAndNames(fileNames, streamContext);

                    //String file = DatabaseDescriptor.getDataFileLocationForTable(streamContext.getTable()) + File.separator + newFileName + "-Data.db";
                    if (logger.isDebugEnabled())
                      logger.debug("Received Data from  : " + message.getFrom() + " " + streamContext.getTargetFile() + " " + file);
                    streamContext.setTargetFile(file);
                    addStreamContext(message.getFrom(), streamContext, streamStatus);
                }

                StreamContextManager.registerStreamCompletionHandler(message.getFrom(), new BootstrapCompletionHandler());
                /* Send a bootstrap initiation done message to execute on default stage. */
                if (logger.isDebugEnabled())
                  logger.debug("Sending a bootstrap initiate done message ...");
                Message doneMessage = new Message(FBUtilities.getLocalAddress(), "", StorageService.bootStrapInitiateDoneVerbHandler_, new byte[0] );
                MessagingService.instance().sendOneWay(doneMessage, message.getFrom());
            }
            catch ( IOException ex )
            {
                logger.info(LogUtil.throwableToString(ex));
            }
        }

        String getNewFileNameFromOldContextAndNames(Map<String, String> fileNames,
                StreamContextManager.StreamContext streamContext)
        {
            File sourceFile = new File( streamContext.getTargetFile() );
            String[] piece = FBUtilities.strip(sourceFile.getName(), "-");
            String cfName = piece[0];
            String ssTableNum = piece[1];
            String typeOfFile = piece[2];

            String newFileNameExpanded = fileNames.get( streamContext.getTable() + "-" + cfName + "-" + ssTableNum );
            //Drop type (Data.db) from new FileName
            String newFileName = newFileNameExpanded.replace("Data.db", typeOfFile);
            return DatabaseDescriptor.getDataFileLocationForTable(streamContext.getTable()) + File.separator + newFileName;
        }

        Map<String, String> getNewNames(StreamContextManager.StreamContext[] streamContexts) throws IOException
        {
            /*
             * Mapping for each file with unique CF-i ---> new file name. For eg.
             * for a file with name <CF>-<i>-Data.db there is a corresponding
             * <CF>-<i>-Index.db. We maintain a mapping from <CF>-<i> to a newly
             * generated file name.
            */
            Map<String, String> fileNames = new HashMap<String, String>();
            /* Get the distinct entries from StreamContexts i.e have one entry per Data/Index/Filter file set */
            Set<String> distinctEntries = new HashSet<String>();
            for ( StreamContextManager.StreamContext streamContext : streamContexts )
            {
                String[] pieces = FBUtilities.strip(new File(streamContext.getTargetFile()).getName(), "-");
                distinctEntries.add(streamContext.getTable() + "-" + pieces[0] + "-" + pieces[1] );
            }

            /* Generate unique file names per entry */
            for ( String distinctEntry : distinctEntries )
            {
                String tableName;
                String[] pieces = FBUtilities.strip(distinctEntry, "-");
                tableName = pieces[0];
                Table table = Table.open( tableName );

                ColumnFamilyStore cfStore = table.getColumnFamilyStore(pieces[1]);
                if (logger.isDebugEnabled())
                  logger.debug("Generating file name for " + distinctEntry + " ...");
                fileNames.put(distinctEntry, cfStore.getTempSSTableFileName());
            }

            return fileNames;
        }

        private void addStreamContext(InetAddress host, StreamContextManager.StreamContext streamContext, StreamContextManager.StreamStatus streamStatus)
        {
            if (logger.isDebugEnabled())
              logger.debug("Adding stream context " + streamContext + " for " + host + " ...");
            StreamContextManager.addStreamContext(host, streamContext, streamStatus);
        }
    }

    /**
     * This is the callback handler that is invoked when we have
     * completely been bootstrapped for a single file by a remote host.
     *
     * TODO if we move this into CFS we could make addSSTables private, improving encapsulation.
    */
    private static class BootstrapCompletionHandler implements IStreamComplete
    {
        public void onStreamCompletion(InetAddress host, StreamContextManager.StreamContext streamContext, StreamContextManager.StreamStatus streamStatus) throws IOException
        {
            /* Parse the stream context and the file to the list of SSTables in the associated Column Family Store. */
            if (streamContext.getTargetFile().contains("-Data.db"))
            {
                String tableName = streamContext.getTable();
                File file = new File( streamContext.getTargetFile() );
                String fileName = file.getName();
                String [] temp = fileName.split("-");

                //Open the file to see if all parts are now here
                SSTableReader sstable = null;
                try
                {
                    sstable = SSTableWriter.renameAndOpen(streamContext.getTargetFile());

                    //TODO add a sanity check that this sstable has all its parts and is ok
                    Table.open(tableName).getColumnFamilyStore(temp[0]).addSSTable(sstable);
                    logger.info("Bootstrap added " + sstable.getFilename());
                }
                catch (IOException e)
                {
                    logger.error("Not able to bootstrap with file " + streamContext.getTargetFile(), e);
                }
            }

            if (logger.isDebugEnabled())
              logger.debug("Sending a bootstrap terminate message with " + streamStatus + " to " + host);
            /* Send a StreamStatusMessage object which may require the source node to re-stream certain files. */
            StreamContextManager.StreamStatusMessage streamStatusMessage = new StreamContextManager.StreamStatusMessage(streamStatus);
            Message message = StreamContextManager.StreamStatusMessage.makeStreamStatusMessage(streamStatusMessage);
            MessagingService.instance().sendOneWay(message, host);
            /* If we're done with everything for this host, remove from bootstrap sources */
            if (StreamContextManager.isDone(host))
                StorageService.instance().removeBootstrapSource(host);
        }
    }

    public static class BootstrapTerminateVerbHandler implements IVerbHandler
    {
        private static Logger logger_ = Logger.getLogger( BootstrapTerminateVerbHandler.class );

        public void doVerb(Message message)
        {
            byte[] body = message.getMessageBody();
            DataInputBuffer bufIn = new DataInputBuffer();
            bufIn.reset(body, body.length);

            try
            {
                StreamContextManager.StreamStatusMessage streamStatusMessage = StreamContextManager.StreamStatusMessage.serializer().deserialize(bufIn);
                StreamContextManager.StreamStatus streamStatus = streamStatusMessage.getStreamStatus();

                switch( streamStatus.getAction() )
                {
                    case DELETE:
                        StreamManager.instance(message.getFrom()).finish(streamStatus.getFile());
                        break;

                    case STREAM:
                        if (logger_.isDebugEnabled())
                          logger_.debug("Need to re-stream file " + streamStatus.getFile());
                        StreamManager.instance(message.getFrom()).repeat();
                        break;

                    default:
                        break;
                }
            }
            catch ( IOException ex )
            {
                logger_.info(LogUtil.throwableToString(ex));
            }
        }
    }
}