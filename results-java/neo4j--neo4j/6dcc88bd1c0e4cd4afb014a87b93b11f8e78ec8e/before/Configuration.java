/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.unsafe.impl.batchimport;

import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.pagecache.ConfiguringPageCacheFactory;
import org.neo4j.kernel.impl.util.OsBeanUtil;
import org.neo4j.unsafe.impl.batchimport.staging.Stage;
import org.neo4j.unsafe.impl.batchimport.staging.Step;

import static java.lang.Math.min;
import static java.lang.Math.round;

import static org.neo4j.graphdb.factory.GraphDatabaseSettings.dense_node_threshold;
import static org.neo4j.graphdb.factory.GraphDatabaseSettings.pagecache_memory;
import static org.neo4j.io.ByteUnit.mebiBytes;

/**
 * User controlled configuration for a {@link BatchImporter}.
 */
public interface Configuration
{
    /**
     * File name in which bad entries from the import will end up. This file will be created in the
     * database directory of the imported database, i.e. <into>/bad.log.
     */
    String BAD_FILE_NAME = "bad.log";
    long MAX_PAGE_CACHE_MEMORY = mebiBytes( 240 );
    int DEFAULT_MAX_MEMORY_PERCENT = 90;

    /**
     * A {@link Stage} works with batches going through one or more {@link Step steps} where one or more threads
     * process batches at each {@link Step}. This setting dictates how big the batches that are passed around are.
     */
    default int batchSize()
    {
        return 10_000;
    }

    /**
     * For statistics the average processing time is based on total processing time divided by
     * number of batches processed. A total average is probably not that interesting so this configuration
     * option specifies how many of the latest processed batches counts in the equation above.
     */
    default int movingAverageSize()
    {
        return 100;
    }

    /**
     * Rough max number of processors (CPU cores) simultaneously used in total by importer at any given time.
     * This value should be set while taking the necessary IO threads into account; the page cache and the operating
     * system will require a couple of threads between them, to handle the IO workload the importer generates.
     * Defaults to the value provided by the {@link Runtime#availableProcessors() jvm}. There's a discrete
     * number of threads that needs to be used just to get the very basics of the import working,
     * so for that reason there's no lower bound to this value.
     *   "Processor" in the context of the batch importer is different from "thread" since when discovering
     * how many processors are fully in use there's a calculation where one thread takes up 0 < fraction <= 1
     * of a processor.
     */
    default int maxNumberOfProcessors()
    {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * @return number of relationships threshold for considering a node dense.
     */
    default int denseNodeThreshold()
    {
        return Integer.parseInt( dense_node_threshold.getDefaultValue() );
    }

    /**
     * @return amount of memory to reserve for the page cache. This should just be "enough" for it to be able
     * to sequentially read and write a couple of stores at a time. If configured too high then there will
     * be less memory available for other caches which are critical during the import. Optimal size is
     * estimated to be 100-200 MiB. The importer will figure out an optimal page size from this value,
     * with slightly bigger page size than "normal" random access use cases.
     */
    default long pageCacheMemory()
    {
        // Get the upper bound of what we can get from the default config calculation
        // We even want to limit amount of memory a bit more since we don't need very much during import
        long defaultPageCacheMemory = ConfiguringPageCacheFactory.defaultHeuristicPageCacheMemory();
        return min( MAX_PAGE_CACHE_MEMORY, defaultPageCacheMemory );
    }

    /**
     * @return max memory to use for import cache data structures while importing.
     * This should exclude the memory acquired by this JVM. By default this returns total physical
     * memory on the machine it's running on minus the max memory of this JVM.
     * {@value #DEFAULT_MAX_MEMORY_PERCENT}% of that figure.
     * @throws UnsupportedOperationException if available memory couldn't be determined.
     */
    default long maxMemoryUsage()
    {
        return calculateMaxMemoryFromPercent( DEFAULT_MAX_MEMORY_PERCENT );
    }

    Configuration DEFAULT = new Configuration()
    {
    };

    class Overridden implements Configuration
    {
        private final Configuration defaults;
        private final Config config;

        public Overridden( Configuration defaults )
        {
            this( defaults, Config.empty() );
        }

        public Overridden( Configuration defaults, Config config )
        {
            this.defaults = defaults;
            this.config = config;
        }

        public Overridden( Config config )
        {
            this( Configuration.DEFAULT, config );
        }

        @Override
        public long pageCacheMemory()
        {
            Long pageCacheMemory = config.get( pagecache_memory );
            if ( pageCacheMemory == null )
            {
                pageCacheMemory = ConfiguringPageCacheFactory.defaultHeuristicPageCacheMemory();
            }
            return min( MAX_PAGE_CACHE_MEMORY, pageCacheMemory );
        }

        @Override
        public int denseNodeThreshold()
        {
            return config.get( dense_node_threshold );
        }

        @Override
        public int movingAverageSize()
        {
            return defaults.movingAverageSize();
        }
    }

    public static Configuration withBatchSize( Configuration config, int batchSize )
    {
        return new Overridden( config )
        {
            @Override
            public int batchSize()
            {
                return batchSize;
            }
        };
    }

    static long calculateMaxMemoryFromPercent( int percent )
    {
        if ( percent < 1 )
        {
            throw new IllegalArgumentException( "Expected percentage to be > 0, was " + percent );
        }
        if ( percent > 100 )
        {
            throw new IllegalArgumentException( "Expected percentage to be < 100, was " + percent );
        }
        long freePhysicalMemory = OsBeanUtil.getFreePhysicalMemory();
        if ( freePhysicalMemory == OsBeanUtil.VALUE_UNAVAILABLE )
        {
            throw new UnsupportedOperationException(
                    "Unable to detect amount of free memory, so max memory has to be explicitly set" );
        }

        double factor = percent / 100D;
        return round( (freePhysicalMemory - Runtime.getRuntime().maxMemory()) * factor );
    }
}