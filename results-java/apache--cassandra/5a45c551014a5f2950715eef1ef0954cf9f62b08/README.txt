commit 5a45c551014a5f2950715eef1ef0954cf9f62b08
Author: Eric Evans <eevans@apache.org>
Date:   Thu Jun 17 21:57:55 2010 +0000

    refactor internal slice methods

    Moved return types to faux-map (list of records) to avoid useless
    conversions to <--> from.

    Patch eevans

    git-svn-id: https://svn.apache.org/repos/asf/cassandra/trunk@955760 13f79535-47bb-0310-9956-ffa450edef68