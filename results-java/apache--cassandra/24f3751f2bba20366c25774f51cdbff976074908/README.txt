commit 24f3751f2bba20366c25774f51cdbff976074908
Author: Eric Evans <eevans@apache.org>
Date:   Tue Jan 5 20:34:21 2010 +0000

    reorganize ThriftGlue

      * use static import of ThriftGlue methods
      * move from o.a.c.glue to o.a.c.service package

    Patch by eevans for CASSANDRA-547

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@896209 13f79535-47bb-0310-9956-ffa450edef68