package com.thinkaurelius.titan.hadoop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.diskstorage.configuration.ConfigElement;
import com.thinkaurelius.titan.hadoop.config.TitanHadoopConfiguration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Tokens {

    private static final Logger log = LoggerFactory.getLogger(Tokens.class);

    public static final String METADATA_RESOURCE = "com/thinkaurelius/titan/hadoop/meta.properties";
    public static final String METADATA_KEY_VERSION = "version";
    public static final String METADATA_KEY_JOB_JAR = "jobjar";

    public static final String VERSION;
    public static final String TITAN_HADOOP_JOB_JAR;

    public static final String TITAN_HADOOP_PIPELINE_MAP_SPILL_OVER = ConfigElement.getPath(TitanHadoopConfiguration.PIPELINE_MAP_SPILL_OVER);
    public static final String TITAN_HADOOP_PIPELINE_TRACK_PATHS = ConfigElement.getPath(TitanHadoopConfiguration.PIPELINE_TRACK_PATHS);
    public static final String TITAN_HADOOP_PIPELINE_TRACK_STATE = ConfigElement.getPath(TitanHadoopConfiguration.PIPELINE_TRACK_STATE);

    public enum Action {DROP, KEEP}

    private static final String NAMESPACE = "titan.hadoop.mapreduce";

    static {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(Tokens.class.getClassLoader().getResourceAsStream(METADATA_RESOURCE));
            Properties meta = new Properties();
            meta.load(isr);

            VERSION = meta.getProperty(METADATA_KEY_VERSION);
            TITAN_HADOOP_JOB_JAR = meta.getProperty(METADATA_KEY_JOB_JAR);

            log.debug("Loaded {}={} from classloader resource {}", METADATA_KEY_VERSION, VERSION, METADATA_RESOURCE);
            log.debug("Loaded {}={} from classloader resource {}", METADATA_KEY_JOB_JAR, TITAN_HADOOP_JOB_JAR, METADATA_RESOURCE);
        } catch (Throwable t) {
            log.error("The Titan/Hadoop version file {} could not be found on the classpath", METADATA_RESOURCE, t);
            throw new RuntimeException(t);
        } finally {
            if (null != isr) {
                try {
                    isr.close();
                } catch (IOException e) {
                    log.warn("Unable to close resource {}", METADATA_RESOURCE, e);
                }
            }
        }
    }

    public static String makeNamespace(final Class klass) {
        return NAMESPACE + "." + klass.getSimpleName().toLowerCase();
    }

    public static final String SETUP = "setup";
    public static final String MAP = "map";
    public static final String REDUCE = "reduce";
    public static final String CLEANUP = "cleanup";
    public static final String _ID = "_id";
    public static final String ID = "id";
    public static final String _PROPERTIES = "_properties";
    public static final String _COUNT = "_count";
    public static final String _LINK = "_link";
    public static final String LABEL = "label";
    public static final String _LABEL = "_label";
    public static final String NULL = "null";
    public static final String TAB = "\t";
    public static final String NEWLINE = "\n";
    public static final String EMPTY_STRING = "";

    public static final String TITAN_HADOOP_HOME = "TITAN_HADOOP_HOME";

    public static final String PART = "part";
    public static final String GRAPH = "graph";
    public static final String SIDEEFFECT = "sideeffect";
    public static final String JOB = "job";

    public static final String BZ2 = "bz2";

    public static int DEFAULT_MAP_SPILL_OVER = 500;

//    public static final String TITAN_HADOOP_PIPELINE_MAP_SPILL_OVER = "titan.hadoop.pipeline.map-spill-over";
//    public static final String TITAN_HADOOP_PIPELINE_TRACK_PATHS = "titan.hadoop.pipeline.track-paths";
//    public static final String TITAN_HADOOP_PIPELINE_TRACK_STATE = "titan.hadoop.pipeline.track-state";

}