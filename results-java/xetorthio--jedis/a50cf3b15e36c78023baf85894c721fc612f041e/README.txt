commit a50cf3b15e36c78023baf85894c721fc612f041e
Author: Jungtaek Lim <kabhwan@gmail.com>
Date:   Wed Jan 22 00:23:40 2014 +0900

    Apply Sentinel runtime configuration API introduced on Redis 2.8.4

    * Implements new sentinel commands (failover, monitor, remove, set)
    * unit test included
    ** added 2 redis-server and 1 sentinel for failover test
    * with some refactoring
    ** SentinelCommands : refactor to have interface
    ** HostAndPortUtil : same format to cluster setup