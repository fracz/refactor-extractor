commit 229f7d9e4436cdafc4e6943dabccf7ca5c0ef5da
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Tue Jul 15 02:36:35 2014 -0400

    Major HBase test refactoring

    This commit was prompted by test failures on titan-hbase-parent
    following the TTL merge.  It's mostly a response to
    https://issues.apache.org/jira/browse/HBASE-10312.  On 0.94 and 0.98,
    this was as simple as updating the HBase version, but 0.96 does not
    have a fixed release yet, so this commit changes the tests to restart
    HBase between each test class under 0.96.