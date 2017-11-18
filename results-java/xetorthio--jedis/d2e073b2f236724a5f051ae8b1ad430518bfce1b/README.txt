commit d2e073b2f236724a5f051ae8b1ad430518bfce1b
Author: Jungtaek Lim <kabhwan@gmail.com>
Date:   Sat Feb 15 10:30:01 2014 +0900

    Use "sentinel failover" to force failover

    * Use "sentinel failover" to force failover
    ** faster than kill redis instances
    * set failover timeout to 1 min
    ** It makes sense with failover within localhost
    * reduce instances : 1 Redis Server and 1 Sentinel
    ** port is not changed -> I'll changed later at end of refactoring