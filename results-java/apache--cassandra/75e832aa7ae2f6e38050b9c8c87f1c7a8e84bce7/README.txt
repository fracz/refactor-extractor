commit 75e832aa7ae2f6e38050b9c8c87f1c7a8e84bce7
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Wed May 12 00:11:54 2010 +0000

    clean up ExpiringMap and improve concurrency w/ NBHM instead of HashTable
    patch by jbellis; reviewed by gdusbabek for CASSANDRA-1077



    git-svn-id: https://svn.apache.org/repos/asf/cassandra/trunk@943345 13f79535-47bb-0310-9956-ffa450edef68