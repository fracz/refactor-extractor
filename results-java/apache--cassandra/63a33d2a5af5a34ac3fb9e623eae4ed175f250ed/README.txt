commit 63a33d2a5af5a34ac3fb9e623eae4ed175f250ed
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Mon May 9 04:04:55 2011 +0000

    refactor to make getX(int) and getX(String) methods take a common code path.  getString no longer [incorrectly] attempts to make sense out of non-String types.  getInt will work on both BigInteger and Long columns.  getBigInteger added to CassandraRS (otherwise you have to use getObject).
    patch by jbellis

    git-svn-id: https://svn.apache.org/repos/asf/cassandra/branches/cassandra-0.8@1100877 13f79535-47bb-0310-9956-ffa450edef68