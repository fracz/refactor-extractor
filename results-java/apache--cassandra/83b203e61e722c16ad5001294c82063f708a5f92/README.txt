commit 83b203e61e722c16ad5001294c82063f708a5f92
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Mon Jun 29 22:07:49 2009 +0000

    add test catching buggy update of header on flush; refactor so there is only one version of code doing those writes (the correct one).
    patch by jbellis; reviewed by Jun Rao for CASSANDRA-264

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@789465 13f79535-47bb-0310-9956-ffa450edef68