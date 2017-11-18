commit e946c6380637b10dc8ec9e257fbfdf47b175dfd3
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Tue Nov 3 02:27:43 2009 +0000

    refactor getDefaultToken into default checking + getRandomToken.  Decorate OPP keys so we don't have to special case IndexedDKs for getSplits.
    patch by jbellis and Vijay Parthasarathy for CASSANDRA-513

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@832272 13f79535-47bb-0310-9956-ffa450edef68