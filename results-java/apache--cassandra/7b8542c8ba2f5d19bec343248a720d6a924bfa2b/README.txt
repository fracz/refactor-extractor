commit 7b8542c8ba2f5d19bec343248a720d6a924bfa2b
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Wed Feb 17 23:22:41 2010 +0000

    refactor to make memtablesPendingFlush a member variable instead of a static, and Memtable to have a reference to CFS instead of table/cfname pair.
    patch by jbellis; reviewed by Stu Hood for CASSANDRA-799

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@911224 13f79535-47bb-0310-9956-ffa450edef68