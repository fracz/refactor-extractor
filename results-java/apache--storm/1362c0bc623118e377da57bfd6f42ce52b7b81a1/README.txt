commit 1362c0bc623118e377da57bfd6f42ce52b7b81a1
Author: Jungtaek Lim <kabhwan@gmail.com>
Date:   Fri Jun 3 13:28:49 2016 +0900

    STORM-1723 Introduce ClusterMetricsConsumer

    * ClusterMetricsConsumer publishes cluster-side related metrics into consumers
      * like MetricsConsumer for topology metrics
    * Users can implement IClusterMetricsConsumer and configure to cluster conf. file to take effect
      * Please refer conf/storm.yaml.example for more details on configuring
      * Nimbus should be launched with additional jars which are needed for IClusterMetricsConsumer
    * Also did some refactor to nimbus.clj