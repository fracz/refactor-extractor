commit f93cdf97ea803c4702023daf1326dc45172de611
Author: johnlenz@google.com <johnlenz@google.com@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Tue Sep 14 23:04:57 2010 +0000

    Minor parse time performance improvement (profiling was showing ~500ms
    spent traversing the obj lit keys).  This changes the order from
    O(keys^2) to O(keys).

    R=nicksantos
    DELTA=47  (29 added, 5 deleted, 13 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=216974


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@428 b0f006be-c8cd-11de-a2e8-8d36a3108c74