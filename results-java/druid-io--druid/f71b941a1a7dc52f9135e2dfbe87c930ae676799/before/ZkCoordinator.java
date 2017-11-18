/*
 * Druid - a distributed column store.
 * Copyright (C) 2012  Metamarkets Group Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.metamx.druid.coordination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metamx.common.lifecycle.LifecycleStart;
import com.metamx.common.lifecycle.LifecycleStop;
import com.metamx.druid.client.DataSegment;
import com.metamx.druid.client.DruidServer;
import com.metamx.druid.curator.announcement.Announcer;
import com.metamx.druid.loading.SegmentLoadingException;
import com.metamx.emitter.EmittingLogger;
import com.metamx.emitter.service.AlertEvent;
import com.metamx.emitter.service.ServiceEmitter;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.cache.ChildData;
import com.netflix.curator.framework.recipes.cache.PathChildrenCache;
import com.netflix.curator.framework.recipes.cache.PathChildrenCacheEvent;
import com.netflix.curator.framework.recipes.cache.PathChildrenCacheListener;
import com.netflix.curator.utils.ZKPaths;

import java.io.File;
import java.io.IOException;

/**
 */
public class ZkCoordinator implements DataSegmentChangeHandler
{
  private static final EmittingLogger log = new EmittingLogger(ZkCoordinator.class);

  private final Object lock = new Object();

  private final ObjectMapper jsonMapper;
  private final ZkCoordinatorConfig config;
  private final DruidServer me;
  private final Announcer announcer;
  private final CuratorFramework curator;
  private final ServerManager serverManager;
  private final ServiceEmitter emitter;

  private final String loadQueueLocation;
  private final String servedSegmentsLocation;

  private volatile PathChildrenCache loadQueueCache;
  private volatile boolean started;

  public ZkCoordinator(
      ObjectMapper jsonMapper,
      ZkCoordinatorConfig config,
      DruidServer me,
      Announcer announcer,
      CuratorFramework curator,
      ServerManager serverManager,
      ServiceEmitter emitter
  )
  {
    this.jsonMapper = jsonMapper;
    this.config = config;
    this.me = me;
    this.announcer = announcer;
    this.curator = curator;
    this.serverManager = serverManager;
    this.emitter = emitter;

    this.loadQueueLocation = ZKPaths.makePath(config.getLoadQueueLocation(), me.getName());
    this.servedSegmentsLocation = ZKPaths.makePath(config.getServedSegmentsLocation(), me.getName());
  }

  @LifecycleStart
  public void start() throws IOException
  {
    log.info("Starting zkCoordinator for server[%s] with config[%s]", me, config);
    synchronized (lock) {
      if (started) {
        return;
      }

      loadQueueCache = new PathChildrenCache(
          curator,
          loadQueueLocation,
          true,
          true,
          new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ZkCoordinator-%s").build()
      );

      try {
        this.config.getSegmentInfoCacheDirectory().mkdirs();

        curator.newNamespaceAwareEnsurePath(loadQueueLocation).ensure(curator.getZookeeperClient());
        curator.newNamespaceAwareEnsurePath(servedSegmentsLocation).ensure(curator.getZookeeperClient());

        loadCache();

        announcer.announce(
            ZKPaths.makePath(config.getAnnounceLocation(), me.getName()),
            jsonMapper.writeValueAsBytes(me.getStringProps())
        );

        loadQueueCache.getListenable().addListener(
            new PathChildrenCacheListener()
            {
              @Override
              public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception
              {
                final ChildData child = event.getData();
                switch (event.getType()) {
                  case CHILD_ADDED:
                    final String path = child.getPath();
                    final DataSegmentChangeRequest segment = jsonMapper.readValue(
                        child.getData(), DataSegmentChangeRequest.class
                    );

                    log.info("New node[%s] with segmentClass[%s]", path, segment.getClass());

                    try {
                      segment.go(ZkCoordinator.this);
                      curator.delete().guaranteed().forPath(path);

                      log.info("Completed processing for node[%s]", path);
                    }
                    catch (Exception e) {
                      try {
                        curator.delete().guaranteed().forPath(path);
                      }
                      catch (Exception e1) {
                        log.info(e1, "Failed to delete node[%s], but ignoring exception.", path);
                      }

                      log.makeAlert(e, "Segment load/unload: uncaught exception.")
                          .addData("node", path)
                          .addData("nodeProperties", segment)
                          .emit();
                    }

                    break;
                  case CHILD_REMOVED:
                    log.info("%s was removed", event.getData().getPath());
                    break;
                  default:
                    log.info("Ignoring event[%s]", event);
                }
              }
            }
        );
      }
      catch (Exception e) {
        Throwables.propagateIfPossible(e, IOException.class);
        throw Throwables.propagate(e);
      }

      started = true;
    }
  }

  @LifecycleStop
  public void stop()
  {
    log.info("Stopping ZkCoordinator with config[%s]", config);
    synchronized (lock) {
      if (!started) {
        return;
      }


      try {
        loadQueueCache.close();

        curator.delete().guaranteed().forPath(ZKPaths.makePath(config.getAnnounceLocation(), me.getName()));
      }
      catch (Exception e) {
        throw Throwables.propagate(e);
      }
      finally {
        loadQueueCache = null;
        started = false;
      }
    }
  }

  private void loadCache()
  {
    File baseDir = config.getSegmentInfoCacheDirectory();
    if (!baseDir.exists()) {
      return;
    }

    for (File file : baseDir.listFiles()) {
      log.info("Loading segment cache file [%s]", file);
      try {
        DataSegment segment = jsonMapper.readValue(file, DataSegment.class);
        if (serverManager.isSegmentCached(segment)) {
          addSegment(segment);
        } else {
          log.warn("Unable to find cache file for %s. Deleting lookup entry", segment.getIdentifier());

          File segmentInfoCacheFile = new File(config.getSegmentInfoCacheDirectory(), segment.getIdentifier());
          if (!segmentInfoCacheFile.delete()) {
            log.warn("Unable to delete segmentInfoCacheFile[%s]", segmentInfoCacheFile);
          }
        }
      }
      catch (Exception e) {
        log.error(e, "Exception occurred reading file [%s]", file);
        emitter.emit(
            new AlertEvent.Builder().build(
                "Failed to read segment info file",
                ImmutableMap.<String, Object>builder()
                            .put("file", file)
                            .put("exception", e.toString())
                            .build()
            )
        );
      }
    }
  }

  @Override
  public void addSegment(DataSegment segment)
  {
    try {
      serverManager.loadSegment(segment);

      File segmentInfoCacheFile = new File(config.getSegmentInfoCacheDirectory(), segment.getIdentifier());
      try {
        jsonMapper.writeValue(segmentInfoCacheFile, segment);
      }
      catch (IOException e) {
        removeSegment(segment);
        throw new SegmentLoadingException(
            "Failed to write to disk segment info cache file[%s]", segmentInfoCacheFile
        );
      }

      announcer.announce(makeSegmentPath(segment), jsonMapper.writeValueAsBytes(segment));
    }
    catch (SegmentLoadingException e) {
      log.makeAlert(e, "Failed to load segment for dataSource")
          .addData("segment", segment)
          .emit();
    }
    catch (JsonProcessingException e) {
      log.makeAlert(e, "WTF, exception writing into byte[]???")
         .addData("segment", segment)
         .emit();
    }
  }

  @Override
  public void removeSegment(DataSegment segment)
  {
    try {
      announcer.unannounce(makeSegmentPath(segment));

      serverManager.dropSegment(segment);

      File segmentInfoCacheFile = new File(config.getSegmentInfoCacheDirectory(), segment.getIdentifier());
      if (!segmentInfoCacheFile.delete()) {
        log.warn("Unable to delete segmentInfoCacheFile[%s]", segmentInfoCacheFile);
      }
    }
    catch (Exception e) {
      log.makeAlert("Failed to remove segment")
          .addData("segment", segment)
          .emit();
    }
  }

  private String makeSegmentPath(DataSegment segment)
  {
    return ZKPaths.makePath(servedSegmentsLocation, segment.getIdentifier());
  }
}