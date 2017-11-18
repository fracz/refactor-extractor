commit d293b77bedb0d196065f6b5a93d086923b6a9114
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Fri Sep 18 19:02:49 2009 +0000

    serialize row outside of commitlog executor to improve parallelizability
    patch by jbellis; reviewed by junrao for CASSANDRA-444

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@816745 13f79535-47bb-0310-9956-ffa450edef68