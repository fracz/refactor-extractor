commit e11fe50bc74a644a97908b84e80a568b2a9e5f35
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Fri Aug 5 20:19:45 2011 +0000

    refactorings and corner-case bug fixes:
    - avoid modifying the List of rows after passing it to a LazilyCompactedRow
    - account for possibility that all data compacted by LCR has expired
    - clean up code duplication around shouldPurge cleanup
    patch by jbellis; reviewed by slebresne for CASSANDRA-2901

    git-svn-id: https://svn.apache.org/repos/asf/cassandra/branches/cassandra-0.8@1154369 13f79535-47bb-0310-9956-ffa450edef68