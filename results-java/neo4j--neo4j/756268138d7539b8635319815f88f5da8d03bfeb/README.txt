commit 756268138d7539b8635319815f88f5da8d03bfeb
Author: Thomas Baum <thomas.baum@atns.de>
Date:   Wed Sep 14 14:42:32 2011 +0200

    rrd changes

    - use seconds instead of milliseconds
    - refactor rrd-samplerbase to use direct java instances instead of jmx interface
    - update rrd4j to 2.0.7
    - update webadmin to use seconds for monitor/rrd requests
    - update neo4js (using seconds)

    !!! ATTENTION: rrd-archives are not compatible, old archives will be renamed/droped