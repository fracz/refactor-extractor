commit ef2f631ebef835d3fe9cfc80f1d31b2f312acedb
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Wed Apr 29 02:01:56 2009 +0000

    fix regression introduced by ReadCommand refactor (get_slice can return column_t from a standard cf, or from a supercolumn in a super cf).  patch by jbellis.

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@769629 13f79535-47bb-0310-9956-ffa450edef68