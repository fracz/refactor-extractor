package storm.kafka.trident;

import backtype.storm.metric.api.CombinedMetric;
import backtype.storm.metric.api.IMetric;
import backtype.storm.metric.api.ReducedMetric;
import backtype.storm.utils.Utils;
import com.google.common.collect.ImmutableMap;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.Message;
import kafka.message.MessageAndOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.kafka.*;
import storm.trident.operation.TridentCollector;

import java.net.ConnectException;
import java.util.*;

public class KafkaUtils {
    public static final Logger LOG = LoggerFactory.getLogger(KafkaUtils.class);
	private static final int NO_OFFSET = -5;


	public static IBrokerReader makeBrokerReader(Map stormConf, KafkaConfig conf) {
		if(conf.hosts instanceof StaticHosts) {
			return new StaticBrokerReader(((StaticHosts) conf.hosts).getPartitionInformation());
		} else {
			return new ZkBrokerReader(stormConf, conf.topic, (ZkHosts) conf.hosts);
		}
	}


     public static Map emitPartitionBatchNew(TridentKafkaConfig config, SimpleConsumer consumer, GlobalPartitionId partition, TridentCollector collector, Map lastMeta, String topologyInstanceId, String topologyName,
                                               ReducedMetric meanMetric, CombinedMetric maxMetric) {
         long offset;
         if(lastMeta!=null) {
             String lastInstanceId = null;
             Map lastTopoMeta = (Map) lastMeta.get("topology");
             if(lastTopoMeta!=null) {
                 lastInstanceId = (String) lastTopoMeta.get("id");
             }
             if(config.forceFromStart && !topologyInstanceId.equals(lastInstanceId)) {
                 offset = getOffset(consumer, config.topic, partition.partition, config.startOffsetTime);
             } else {
                 offset = (Long) lastMeta.get("nextOffset");
             }
         } else {
             long startTime = -1;
             if(config.forceFromStart) startTime = config.startOffsetTime;
             offset = getOffset(consumer, config.topic, partition.partition, startTime);
         }
         ByteBufferMessageSet msgs;
         try {
            long start = System.nanoTime();
			FetchRequestBuilder builder = new FetchRequestBuilder();
			FetchRequest fetchRequest = builder.addFetch(config.topic, partition.partition, offset, config.fetchSizeBytes).build();
			msgs = consumer.fetch(fetchRequest).messageSet(config.topic, partition.partition);
            long end = System.nanoTime();
            long millis = (end - start) / 1000000;
            meanMetric.update(millis);
            maxMetric.update(millis);
         } catch(Exception e) {
             if(e instanceof ConnectException) {
                 throw new FailedFetchException(e);
             } else {
                 throw new RuntimeException(e);
             }
         }
         long endoffset = offset;
         for(MessageAndOffset msg: msgs) {
             emit(config, collector, msg.message());
             endoffset = msg.nextOffset();
         }
         Map newMeta = new HashMap();
         newMeta.put("offset", offset);
         newMeta.put("nextOffset", endoffset);
         newMeta.put("instanceId", topologyInstanceId);
         newMeta.put("partition", partition.partition);
         newMeta.put("broker", ImmutableMap.of("host", partition.host.host, "port", partition.host.port));
         newMeta.put("topic", config.topic);
         newMeta.put("topology", ImmutableMap.of("name", topologyName, "id", topologyInstanceId));
         return newMeta;
     }

     public static void emit(TridentKafkaConfig config, TridentCollector collector, Message msg) {
         Iterable<List<Object>> values =
             config.scheme.deserialize(Utils.toByteArray(msg.payload()));
         if(values!=null) {
             for(List<Object> value: values)
                 collector.emit(value);
         }
     }


	public static long getOffset(SimpleConsumer consumer, String topic, int partition, long startOffsetTime) {
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
		Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
		requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(startOffsetTime, 1));
		OffsetRequest request = new OffsetRequest(
				requestInfo, kafka.api.OffsetRequest.CurrentVersion(), kafka.api.OffsetRequest.DefaultClientId());

		long[] offsets = consumer.getOffsetsBefore(request).offsets(topic, partition);
		if ( offsets.length > 0) {
			return offsets[0];
		} else {
			return NO_OFFSET;
		}
	}

	public static class KafkaOffsetMetric implements IMetric {
        Map<GlobalPartitionId, Long> _partitionToOffset = new HashMap<GlobalPartitionId, Long>();
        Set<GlobalPartitionId> _partitions;
        String _topic;
        DynamicPartitionConnections _connections;

        public KafkaOffsetMetric(String topic, DynamicPartitionConnections connections) {
            _topic = topic;
            _connections = connections;
        }

        public void setLatestEmittedOffset(GlobalPartitionId partition, long offset) {
            _partitionToOffset.put(partition, offset);
        }

        @Override
        public Object getValueAndReset() {
            try {
                long totalSpoutLag = 0;
                long totalLatestTimeOffset = 0;
                long totalLatestEmittedOffset = 0;
                HashMap ret = new HashMap();
                if(_partitions != null && _partitions.size() == _partitionToOffset.size()) {
                    for(Map.Entry<GlobalPartitionId, Long> e : _partitionToOffset.entrySet()) {
                        GlobalPartitionId partition = e.getKey();
                        SimpleConsumer consumer = _connections.getConnection(partition);
                        if(consumer == null) {
                            LOG.warn("partitionToOffset contains partition not found in _connections. Stale partition data?");
                            return null;
                        }
                        long latestTimeOffset = getOffset(consumer, _topic, partition.partition, kafka.api.OffsetRequest.LatestTime());
                        if(latestTimeOffset == 0) {
                            LOG.warn("No data found in Kafka Partition " + partition.getId());
                            return null;
                        }
                        long latestEmittedOffset = e.getValue();
                        long spoutLag = latestTimeOffset - latestEmittedOffset;
                        ret.put(partition.getId() + "/" + "spoutLag", spoutLag);
                        ret.put(partition.getId() + "/" + "latestTime", latestTimeOffset);
                        ret.put(partition.getId() + "/" + "latestEmittedOffset", latestEmittedOffset);
                        totalSpoutLag += spoutLag;
                        totalLatestTimeOffset += latestTimeOffset;
                        totalLatestEmittedOffset += latestEmittedOffset;
                    }
                    ret.put("totalSpoutLag", totalSpoutLag);
                    ret.put("totalLatestTime", totalLatestTimeOffset);
                    ret.put("totalLatestEmittedOffset", totalLatestEmittedOffset);
                    return ret;
                } else {
                    LOG.info("Metrics Tick: Not enough data to calculate spout lag.");
                }
            } catch(Throwable t) {
                LOG.warn("Metrics Tick: Exception when computing kafkaOffset metric.", t);
            }
            return null;
        }

       public void refreshPartitions(Set<GlobalPartitionId> partitions) {
           _partitions = partitions;
           Iterator<GlobalPartitionId> it = _partitionToOffset.keySet().iterator();
           while(it.hasNext()) {
               if(!partitions.contains(it.next())) it.remove();
           }
       }
    };
}