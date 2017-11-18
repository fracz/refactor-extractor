commit 761e953afa4a10831a1675bbc6c0fb26a09588fb
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Mon May 11 14:26:16 2009 +0000

    clean up compaction code and refactor to allow user-specified threadhold (minimum number of CFs to compact).  patch by jbellis

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@773573 13f79535-47bb-0310-9956-ffa450edef68