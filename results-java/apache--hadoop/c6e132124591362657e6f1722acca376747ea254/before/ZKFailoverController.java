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
package org.apache.hadoop.ha;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.HadoopIllegalArgumentException;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ha.ActiveStandbyElector.ActiveStandbyElectorCallback;
import org.apache.hadoop.ha.HAServiceProtocol.StateChangeRequestInfo;
import org.apache.hadoop.ha.HAServiceProtocol.RequestSource;
import org.apache.hadoop.ha.HAZKUtil.ZKAuthInfo;
import org.apache.hadoop.ha.HealthMonitor.State;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.util.Tool;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.hadoop.util.ToolRunner;
import org.apache.zookeeper.data.ACL;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

@InterfaceAudience.LimitedPrivate("HDFS")
public abstract class ZKFailoverController implements Tool {

  static final Log LOG = LogFactory.getLog(ZKFailoverController.class);

  public static final String ZK_QUORUM_KEY = "ha.zookeeper.quorum";
  private static final String ZK_SESSION_TIMEOUT_KEY = "ha.zookeeper.session-timeout.ms";
  private static final int ZK_SESSION_TIMEOUT_DEFAULT = 5*1000;
  private static final String ZK_PARENT_ZNODE_KEY = "ha.zookeeper.parent-znode";
  public static final String ZK_ACL_KEY = "ha.zookeeper.acl";
  private static final String ZK_ACL_DEFAULT = "world:anyone:rwcda";
  public static final String ZK_AUTH_KEY = "ha.zookeeper.auth";
  static final String ZK_PARENT_ZNODE_DEFAULT = "/hadoop-ha";

  /**
   * All of the conf keys used by the ZKFC. This is used in order to allow
   * them to be overridden on a per-nameservice or per-namenode basis.
   */
  protected static final String[] ZKFC_CONF_KEYS = new String[] {
    ZK_QUORUM_KEY,
    ZK_SESSION_TIMEOUT_KEY,
    ZK_PARENT_ZNODE_KEY,
    ZK_ACL_KEY,
    ZK_AUTH_KEY
  };


  /** Unable to format the parent znode in ZK */
  static final int ERR_CODE_FORMAT_DENIED = 2;
  /** The parent znode doesn't exist in ZK */
  static final int ERR_CODE_NO_PARENT_ZNODE = 3;
  /** Fencing is not properly configured */
  static final int ERR_CODE_NO_FENCER = 4;
  /** Automatic failover is not enabled */
  static final int ERR_CODE_AUTO_FAILOVER_NOT_ENABLED = 5;

  private Configuration conf;

  private HealthMonitor healthMonitor;
  private ActiveStandbyElector elector;

  private HAServiceTarget localTarget;

  private State lastHealthState = State.INITIALIZING;

  /** Set if a fatal error occurs */
  private String fatalError = null;

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
    localTarget = getLocalTarget();
  }


  protected abstract byte[] targetToData(HAServiceTarget target);
  protected abstract HAServiceTarget getLocalTarget();
  protected abstract HAServiceTarget dataToTarget(byte[] data);
  protected abstract void loginAsFCUser() throws IOException;

  /**
   * Return the name of a znode inside the configured parent znode in which
   * the ZKFC will do all of its work. This is so that multiple federated
   * nameservices can run on the same ZK quorum without having to manually
   * configure them to separate subdirectories.
   */
  protected abstract String getScopeInsideParentNode();

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public int run(final String[] args) throws Exception {
    if (!localTarget.isAutoFailoverEnabled()) {
      LOG.fatal("Automatic failover is not enabled for " + localTarget + "." +
          " Please ensure that automatic failover is enabled in the " +
          "configuration before running the ZK failover controller.");
      return ERR_CODE_AUTO_FAILOVER_NOT_ENABLED;
    }
    loginAsFCUser();
    try {
      return SecurityUtil.doAsLoginUserOrFatal(new PrivilegedAction<Integer>() {
        @Override
        public Integer run() {
          try {
            return doRun(args);
          } catch (Exception t) {
            throw new RuntimeException(t);
          }
        }
      });
    } catch (RuntimeException rte) {
      throw (Exception)rte.getCause();
    }
  }


  private int doRun(String[] args)
      throws HadoopIllegalArgumentException, IOException, InterruptedException {
    initZK();
    if (args.length > 0) {
      if ("-formatZK".equals(args[0])) {
        boolean force = false;
        boolean interactive = true;
        for (int i = 1; i < args.length; i++) {
          if ("-force".equals(args[i])) {
            force = true;
          } else if ("-nonInteractive".equals(args[i])) {
            interactive = false;
          } else {
            badArg(args[i]);
          }
        }
        return formatZK(force, interactive);
      } else {
        badArg(args[0]);
      }
    }

    if (!elector.parentZNodeExists()) {
      LOG.fatal("Unable to start failover controller. " +
          "Parent znode does not exist.\n" +
          "Run with -formatZK flag to initialize ZooKeeper.");
      return ERR_CODE_NO_PARENT_ZNODE;
    }

    try {
      localTarget.checkFencingConfigured();
    } catch (BadFencingConfigurationException e) {
      LOG.fatal("Fencing is not configured for " + localTarget + ".\n" +
          "You must configure a fencing method before using automatic " +
          "failover.", e);
      return ERR_CODE_NO_FENCER;
    }

    initHM();
    try {
      mainLoop();
    } finally {
      elector.quitElection(true);
      healthMonitor.shutdown();
      healthMonitor.join();
    }
    return 0;
  }

  private void badArg(String arg) {
    printUsage();
    throw new HadoopIllegalArgumentException(
        "Bad argument: " + arg);
  }

  private void printUsage() {
    System.err.println("Usage: " + this.getClass().getSimpleName() +
        " [-formatZK [-force | -nonInteractive]]");
  }

  private int formatZK(boolean force, boolean interactive)
      throws IOException, InterruptedException {
    if (elector.parentZNodeExists()) {
      if (!force && (!interactive || !confirmFormat())) {
        return ERR_CODE_FORMAT_DENIED;
      }

      try {
        elector.clearParentZNode();
      } catch (IOException e) {
        LOG.error("Unable to clear zk parent znode", e);
        return 1;
      }
    }

    elector.ensureParentZNode();
    return 0;
  }

  private boolean confirmFormat() {
    String parentZnode = getParentZnode();
    System.err.println(
        "===============================================\n" +
        "The configured parent znode " + parentZnode + " already exists.\n" +
        "Are you sure you want to clear all failover information from\n" +
        "ZooKeeper?\n" +
        "WARNING: Before proceeding, ensure that all HDFS services and\n" +
        "failover controllers are stopped!\n" +
        "===============================================");
    try {
      return ToolRunner.confirmPrompt("Proceed formatting " + parentZnode + "?");
    } catch (IOException e) {
      LOG.debug("Failed to confirm", e);
      return false;
    }
  }

  // ------------------------------------------
  // Begin actual guts of failover controller
  // ------------------------------------------

  private void initHM() {
    healthMonitor = new HealthMonitor(conf, localTarget);
    healthMonitor.addCallback(new HealthCallbacks());
    healthMonitor.start();
  }

  private void initZK() throws HadoopIllegalArgumentException, IOException {
    String zkQuorum = conf.get(ZK_QUORUM_KEY);
    int zkTimeout = conf.getInt(ZK_SESSION_TIMEOUT_KEY,
        ZK_SESSION_TIMEOUT_DEFAULT);
    // Parse ACLs from configuration.
    String zkAclConf = conf.get(ZK_ACL_KEY, ZK_ACL_DEFAULT);
    zkAclConf = HAZKUtil.resolveConfIndirection(zkAclConf);
    List<ACL> zkAcls = HAZKUtil.parseACLs(zkAclConf);
    if (zkAcls.isEmpty()) {
      zkAcls = Ids.CREATOR_ALL_ACL;
    }

    // Parse authentication from configuration.
    String zkAuthConf = conf.get(ZK_AUTH_KEY);
    zkAuthConf = HAZKUtil.resolveConfIndirection(zkAuthConf);
    List<ZKAuthInfo> zkAuths;
    if (zkAuthConf != null) {
      zkAuths = HAZKUtil.parseAuth(zkAuthConf);
    } else {
      zkAuths = Collections.emptyList();
    }

    // Sanity check configuration.
    Preconditions.checkArgument(zkQuorum != null,
        "Missing required configuration '%s' for ZooKeeper quorum",
        ZK_QUORUM_KEY);
    Preconditions.checkArgument(zkTimeout > 0,
        "Invalid ZK session timeout %s", zkTimeout);


    elector = new ActiveStandbyElector(zkQuorum,
        zkTimeout, getParentZnode(), zkAcls, zkAuths,
        new ElectorCallbacks());
  }

  private String getParentZnode() {
    String znode = conf.get(ZK_PARENT_ZNODE_KEY,
        ZK_PARENT_ZNODE_DEFAULT);
    if (!znode.endsWith("/")) {
      znode += "/";
    }
    return znode + getScopeInsideParentNode();
  }

  private synchronized void mainLoop() throws InterruptedException {
    while (fatalError == null) {
      wait();
    }
    assert fatalError != null; // only get here on fatal
    throw new RuntimeException(
        "ZK Failover Controller failed: " + fatalError);
  }

  private synchronized void fatalError(String err) {
    LOG.fatal("Fatal error occurred:" + err);
    fatalError = err;
    notifyAll();
  }

  private synchronized void becomeActive() throws ServiceFailedException {
    LOG.info("Trying to make " + localTarget + " active...");
    try {
      HAServiceProtocolHelper.transitionToActive(localTarget.getProxy(
          conf, FailoverController.getRpcTimeoutToNewActive(conf)),
          createReqInfo());
      LOG.info("Successfully transitioned " + localTarget +
          " to active state");
    } catch (Throwable t) {
      LOG.fatal("Couldn't make " + localTarget + " active", t);
      if (t instanceof ServiceFailedException) {
        throw (ServiceFailedException)t;
      } else {
        throw new ServiceFailedException("Couldn't transition to active",
            t);
      }
/*
* TODO:
* we need to make sure that if we get fenced and then quickly restarted,
* none of these calls will retry across the restart boundary
* perhaps the solution is that, whenever the nn starts, it gets a unique
* ID, and when we start becoming active, we record it, and then any future
* calls use the same ID
*/

    }
  }

  private StateChangeRequestInfo createReqInfo() {
    return new StateChangeRequestInfo(RequestSource.REQUEST_BY_ZKFC);
  }

  private synchronized void becomeStandby() {
    LOG.info("ZK Election indicated that " + localTarget +
        " should become standby");
    try {
      int timeout = FailoverController.getGracefulFenceTimeout(conf);
      localTarget.getProxy(conf, timeout).transitionToStandby(createReqInfo());
      LOG.info("Successfully transitioned " + localTarget +
          " to standby state");
    } catch (Exception e) {
      LOG.error("Couldn't transition " + localTarget + " to standby state",
          e);
      // TODO handle this. It's a likely case since we probably got fenced
      // at the same time.
    }
  }

  /**
   * @return the last health state passed to the FC
   * by the HealthMonitor.
   */
  @VisibleForTesting
  State getLastHealthState() {
    return lastHealthState;
  }

  @VisibleForTesting
  ActiveStandbyElector getElectorForTests() {
    return elector;
  }

  /**
   * Callbacks from elector
   */
  class ElectorCallbacks implements ActiveStandbyElectorCallback {
    @Override
    public void becomeActive() throws ServiceFailedException {
      ZKFailoverController.this.becomeActive();
    }

    @Override
    public void becomeStandby() {
      ZKFailoverController.this.becomeStandby();
    }

    @Override
    public void enterNeutralMode() {
    }

    @Override
    public void notifyFatalError(String errorMessage) {
      fatalError(errorMessage);
    }

    @Override
    public void fenceOldActive(byte[] data) {
      HAServiceTarget target = dataToTarget(data);

      LOG.info("Should fence: " + target);
      boolean gracefulWorked = new FailoverController(conf,
          RequestSource.REQUEST_BY_ZKFC).tryGracefulFence(target);
      if (gracefulWorked) {
        // It's possible that it's in standby but just about to go into active,
        // no? Is there some race here?
        LOG.info("Successfully transitioned " + target + " to standby " +
            "state without fencing");
        return;
      }

      try {
        target.checkFencingConfigured();
      } catch (BadFencingConfigurationException e) {
        LOG.error("Couldn't fence old active " + target, e);
        // TODO: see below todo
        throw new RuntimeException(e);
      }

      if (!target.getFencer().fence(target)) {
        // TODO: this will end up in some kind of tight loop,
        // won't it? We need some kind of backoff
        throw new RuntimeException("Unable to fence " + target);
      }
    }

    @Override
    public String toString() {
      return "Elector callbacks for " + localTarget;
    }
  }

  /**
   * Callbacks from HealthMonitor
   */
  class HealthCallbacks implements HealthMonitor.Callback {
    @Override
    public void enteredState(HealthMonitor.State newState) {
      LOG.info("Local service " + localTarget +
          " entered state: " + newState);
      switch (newState) {
      case SERVICE_HEALTHY:
        LOG.info("Joining master election for " + localTarget);
        elector.joinElection(targetToData(localTarget));
        break;

      case INITIALIZING:
        LOG.info("Ensuring that " + localTarget + " does not " +
            "participate in active master election");
        elector.quitElection(false);
        break;

      case SERVICE_UNHEALTHY:
      case SERVICE_NOT_RESPONDING:
        LOG.info("Quitting master election for " + localTarget +
            " and marking that fencing is necessary");
        elector.quitElection(true);
        break;

      case HEALTH_MONITOR_FAILED:
        fatalError("Health monitor failed!");
        break;

      default:
        throw new IllegalArgumentException("Unhandled state:" + newState);
      }

      lastHealthState = newState;
    }
  }
}