commit bcf4dcc666694b80b4ead2adb4a02486f9078e75
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Mon May 4 19:02:01 2009 +0000

    refactor HttpConnection to use its own executor instead of abusing MessagingService.  This will let us refactor Message body to a byte[].
    patch by jbellis; reviewed by nk11 for CASSANDRA-120

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@771400 13f79535-47bb-0310-9956-ffa450edef68