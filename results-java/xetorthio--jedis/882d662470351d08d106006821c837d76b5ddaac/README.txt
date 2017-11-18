commit 882d662470351d08d106006821c837d76b5ddaac
Author: Jungtaek Lim <kabhwan@gmail.com>
Date:   Tue Feb 25 18:29:09 2014 +0900

    Make Jedis Cluster more likely to antirez's redis-rb-cluster

    JedisClusterCommand

    * improvements on connection error handling
    ** if based on slot connection throws connection related exception, retry to random node
    ** if we retry with random node, but all nodes are unreachable, throw JedisConnectionException without retry
    ** try to release connection whether connection is broken or not

    * bug fix : if asking flag is on, and success this time, set asking flag to off

    JedisClusterConnectionHandler

    * have flexibility on initializing slots cache
    ** allow some nodes connection failure - skip
    ** if current node is success initializing slots cache, skip other nodes
    ** if current node failed to initialize slots cache, discard all discovered nodes and slots

    * set nodes if node does not exist in nodes
    ** it restricts JedisPool to replace - prevent IllegalStateException : Returned object not currently part of this pool

    JedisSlotBasedConnectionGuaranteedConnectionHandler

    * getConnection (random connection)
    ** check all connections by random sequence
    ** always return valid connection (able to ping-pong)
    ** throw exception if all connections are invalid

    * some refactoring