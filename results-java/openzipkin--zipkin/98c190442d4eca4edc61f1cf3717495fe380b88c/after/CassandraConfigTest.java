/**
 * Copyright 2015-2016 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin.cassandra;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Authenticator;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LatencyAwarePolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import java.net.InetSocketAddress;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CassandraConfigTest {

  @Test
  public void contactPoints_defaultsToLocalhost() {
    CassandraConfig config = CassandraConfig.builder().build();

    assertThat(config.parseContactPoints())
        .containsExactly(new InetSocketAddress("127.0.0.1", 9042));
  }

  @Test
  public void contactPoints_defaultsToPort9042() {
    CassandraConfig config = CassandraConfig.builder()
        .contactPoints("1.1.1.1")
        .build();

    assertThat(config.parseContactPoints())
        .containsExactly(new InetSocketAddress("1.1.1.1", 9042));
  }

  @Test
  public void contactPoints_defaultsToPort9042_multi() {
    CassandraConfig config = CassandraConfig.builder()
        .contactPoints("1.1.1.1:9143,2.2.2.2")
        .build();

    assertThat(config.parseContactPoints()).containsExactly(
        new InetSocketAddress("1.1.1.1", 9143),
        new InetSocketAddress("2.2.2.2", 9042)
    );
  }

  @Test
  public void contactPoints_hostAndPort() {
    CassandraConfig config = CassandraConfig.builder()
        .contactPoints("1.1.1.1:9142")
        .build();

    assertThat(config.parseContactPoints())
        .containsExactly(new InetSocketAddress("1.1.1.1", 9142));
  }

  @Test
  public void connectPort_singleContactPoint() {
    CassandraConfig config = CassandraConfig.builder()
        .contactPoints("1.1.1.1:9142")
        .build();

    assertThat(config.toCluster().getConfiguration().getProtocolOptions().getPort())
        .isEqualTo(9142);
  }

  @Test
  public void connectPort_whenContactPointsHaveSamePort() {
    CassandraConfig config = CassandraConfig.builder()
        .contactPoints("1.1.1.1:9143,2.2.2.2:9143")
        .build();

    assertThat(config.toCluster().getConfiguration().getProtocolOptions().getPort())
        .isEqualTo(9143);
  }

  @Test
  public void connectPort_whenContactPointsHaveMixedPorts_coercesToDefault() {
    CassandraConfig config = CassandraConfig.builder()
        .contactPoints("1.1.1.1:9143,2.2.2.2")
        .build();

    assertThat(config.toCluster().getConfiguration().getProtocolOptions().getPort())
        .isEqualTo(9042);
  }

  @Test
  public void usernamePassword_impliesNullDelimitedUtf8Bytes() {
    CassandraConfig config = CassandraConfig.builder()
        .username("bob")
        .password("secret")
        .build();

    Authenticator authenticator =
        config.toCluster().getConfiguration().getProtocolOptions().getAuthProvider()
            .newAuthenticator(new InetSocketAddress("localhost", 8080));

    byte[] SASLhandshake = {0, 'b', 'o', 'b', 0, 's', 'e', 'c', 'r', 'e', 't'};
    assertThat(authenticator.initialResponse())
        .isEqualTo(SASLhandshake);
  }

  @Test
  public void authProvider_defaultsToNone() {
    CassandraConfig config = CassandraConfig.builder().build();

    assertThat(config.toCluster().getConfiguration().getProtocolOptions().getAuthProvider())
        .isEqualTo(AuthProvider.NONE);
  }

  @Test
  public void loadBalancing_defaultsToFirstHostDatacenter() {
    CassandraConfig config = CassandraConfig.builder().build();

    DCAwareRoundRobinPolicy policy = toDCAwareRoundRobinPolicy(config);

    Host foo = mock(Host.class);
    when(foo.getDatacenter()).thenReturn("foo");
    Host bar = mock(Host.class);
    when(bar.getDatacenter()).thenReturn("bar");
    policy.init(mock(Cluster.class), asList(foo, bar));

    assertThat(policy.distance(foo)).isEqualTo(HostDistance.LOCAL);
    assertThat(policy.distance(bar)).isEqualTo(HostDistance.IGNORED);
  }

  @Test
  public void loadBalancing_settingLocalDcIgnoresOtherDatacenters() {
    CassandraConfig config = CassandraConfig.builder().localDc("bar").build();

    DCAwareRoundRobinPolicy policy = toDCAwareRoundRobinPolicy(config);

    Host foo = mock(Host.class);
    when(foo.getDatacenter()).thenReturn("foo");
    Host bar = mock(Host.class);
    when(bar.getDatacenter()).thenReturn("bar");
    policy.init(mock(Cluster.class), asList(foo, bar));

    assertThat(policy.distance(foo)).isEqualTo(HostDistance.IGNORED);
    assertThat(policy.distance(bar)).isEqualTo(HostDistance.LOCAL);
  }

  private static DCAwareRoundRobinPolicy toDCAwareRoundRobinPolicy(CassandraConfig config) {
    return (DCAwareRoundRobinPolicy) ((LatencyAwarePolicy) ((TokenAwarePolicy) config.toCluster()
        .getConfiguration()
        .getPolicies()
        .getLoadBalancingPolicy())
        .getChildPolicy()).getChildPolicy();
  }

  @Test
  public void maxConnections_defaultsTo8() {
    CassandraConfig config = CassandraConfig.builder().build();

    PoolingOptions poolingOptions = config.toCluster().getConfiguration().getPoolingOptions();

    assertThat(poolingOptions.getMaxConnectionsPerHost(HostDistance.LOCAL)).isEqualTo(8);
  }

  @Test
  public void maxConnections_setsMaxConnectionsPerDatacenterLocalHost() {
    CassandraConfig config = CassandraConfig.builder().maxConnections(16).build();

    PoolingOptions poolingOptions = config.toCluster().getConfiguration().getPoolingOptions();

    assertThat(poolingOptions.getMaxConnectionsPerHost(HostDistance.LOCAL)).isEqualTo(16);
  }
}