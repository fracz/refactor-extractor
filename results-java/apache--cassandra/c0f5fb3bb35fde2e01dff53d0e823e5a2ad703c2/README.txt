commit c0f5fb3bb35fde2e01dff53d0e823e5a2ad703c2
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Mon Apr 20 20:05:26 2009 +0000

    refactor read path: first we make readColumnFamily accept a ReadCommand, and use that to clean out duplicate code in CassandraServer.  Then we clean up the duplicate versions of the read methods in StorageService by making them ReadCommand-based, too.  [not touching multiget code for now.]
    patch by jbellis; reviewed by Eric Evans for #88

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@766841 13f79535-47bb-0310-9956-ffa450edef68