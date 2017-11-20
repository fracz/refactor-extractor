/*
 * Copyright 2010-2013 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.server.distributed;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.hazelcast.OHazelcastPlugin;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Starts 3 servers, lock on node3 simulating a unknown stall situation, checks:
 * <ul>
 * <li>other nodes can work in the meanwhile</li>
 * <li>node3 is restarted automatically</li>
 * <li>node3 is realigned once backup is finished</li>
 * </ul>
 */
public class RestartNodeTest extends AbstractServerClusterTxTest {
  final static int            SERVERS       = 3;
  volatile boolean            inserting     = true;
  volatile int                serverStarted = 0;
  final private Set<String>   nodeReJoined  = new HashSet<String>();
  final private AtomicInteger nodeLefts     = new AtomicInteger();

  @Test
  public void test() throws Exception {
    startupNodesInSequence = true;
    useTransactions = true;
    count = 500;
    maxRetries = 10;
    delayWriter = 0;
    init(SERVERS);
    prepare(false);

    // EXECUTE TESTS ONLY ON FIRST 2 NODES LEAVING NODE3 AD BACKUP ONLY REPLICA
    executeTestsOnServers = new ArrayList<ServerRun>();
    for (int i = 0; i < serverInstance.size() - 1; ++i) {
      executeTestsOnServers.add(serverInstance.get(i));
    }

    execute();
  }

  @Override
  protected void onServerStarted(final ServerRun server) {
    super.onServerStarted(server);

    if (serverStarted == 0) {
      // INSTALL ON FIRST SERVER ONLY THE SERVER MONITOR TO CHECK IF HAS BEEN RESTARTED
      server.server.getDistributedManager().registerLifecycleListener(new ODistributedLifecycleListener() {
        @Override
        public boolean onNodeJoining(String iNode) {
          return true;
        }

        @Override
        public void onNodeJoined(String iNode) {
          nodeReJoined.add(iNode);
        }

        @Override
        public void onNodeLeft(String iNode) {
          nodeReJoined.clear();
          nodeLefts.incrementAndGet();
        }

        @Override
        public void onDatabaseChangeStatus(String iNode, String iDatabaseName, ODistributedServerManager.DB_STATUS iNewStatus) {
        }
      });
    }

    if (serverStarted++ == (SERVERS - 1)) {

      // BACKUP LAST SERVER, RUN ASYNCHRONOUSLY
      new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            // CRASH LAST SERVER try {
            executeWhen(new Callable<Boolean>() {
              // CONDITION
              @Override
              public Boolean call() throws Exception {
                final ODatabaseDocumentTx database = poolFactory.get(getDatabaseURL(serverInstance.get(0)), "admin", "admin")
                    .acquire();
                try {
                  return database.countClass("Person") > (count * SERVERS) * 1 / 4;
                } finally {
                  database.close();
                }
              }
            }, // ACTION
                new Callable() {
              @Override
              public Object call() throws Exception {
                Assert.assertTrue("Insert was too fast", inserting);

                banner("RESTARTING SERVER " + (SERVERS - 1));

                final String nodeName = server.server.getDistributedManager().getLocalNodeName();
                ((OHazelcastPlugin) serverInstance.get(0).getServerInstance().getDistributedManager()).restartNode(nodeName);

                Thread.sleep(3000);

                // WAIT UNTIL THE NODE IS UP & RUNNING
                while (!(serverInstance.get(0).getServerInstance().getDistributedManager()).isNodeOnline(nodeName,
                    getDatabaseName())) {
                  Thread.sleep(1000);
                }

                // WAIT UNTIL THE END
                final ODatabaseDocumentTx database = poolFactory.get(getDatabaseURL(serverInstance.get(0)), "admin", "admin")
                    .acquire();
                try {
                  while (database.countClass("Person") < count * SERVERS)
                    Thread.sleep(1000);
                } finally {
                  database.close();
                }

                return null;
              }
            });

          } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error on execution flow");
          }
        }

      }).start();
    }
  }

  @Override
  protected void onAfterExecution() throws Exception {
    inserting = false;
    Assert.assertEquals("Node was not restarted", 1, nodeReJoined.size());
    Assert.assertEquals("Found no node has been restarted", 1, nodeLefts.get());
  }

  protected String getDatabaseURL(final ServerRun server) {
    return "remote:" + server.getBinaryProtocolAddress() + "/" + getDatabaseName();
  }

  @Override
  public String getDatabaseName() {
    return "distributed-restartnode";
  }
}