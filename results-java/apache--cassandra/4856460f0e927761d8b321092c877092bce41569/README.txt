commit 4856460f0e927761d8b321092c877092bce41569
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Tue Oct 27 14:40:29 2009 +0000

    refactor bootstrap to only concern itself with bootstrapping the local node, which greatly simplifies things
    patch by jbellis; reviewed by goffinet for CASSANDRA-483

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@830214 13f79535-47bb-0310-9956-ffa450edef68