commit a4e9b14d21473e862144ddd9198ad7e240e2e614
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Wed Feb 17 23:21:57 2010 +0000

    refactor IFlushable contract to push differences b/t Mt and BMT into their respective classes
    patch by jbellis; reviewed by Stu Hood for CASSANDRA-799

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@911222 13f79535-47bb-0310-9956-ffa450edef68