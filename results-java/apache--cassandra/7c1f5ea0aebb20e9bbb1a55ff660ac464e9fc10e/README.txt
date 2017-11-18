commit 7c1f5ea0aebb20e9bbb1a55ff660ac464e9fc10e
Author: Eric Evans <eevans@apache.org>
Date:   Sun Oct 31 14:37:27 2010 +0000

    refactor QueryProcessor

    - encapsulate the different reads better
    - eliminate duplicate code

    Patch by eevans

    git-svn-id: https://svn.apache.org/repos/asf/cassandra/trunk@1029365 13f79535-47bb-0310-9956-ffa450edef68