commit 0c0c6e5fda0d264b927da379a93194b5e7f21ceb
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Fri Apr 17 20:17:16 2009 +0000

    refactor Filter heirarchy, making hash generation easily customizable.  Use Murmur
    hash + combinatorics to generate hashes.

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@766140 13f79535-47bb-0310-9956-ffa450edef68