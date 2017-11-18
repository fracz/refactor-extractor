commit 28cf5d73305a2921b9f46a42b1975ce47be001fa
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Mon Apr 6 15:10:19 2009 +0000

    refactor SF.next(String key, DataOutputBuffer bufOut, String cf, Coordinate section) to call its overload to reduce code duplication. this will make auditing the partitioner changes easier.  patch by jbellis; reviewed by Jun Rau.  see #52

    git-svn-id: https://svn.apache.org/repos/asf/incubator/cassandra/trunk@762378 13f79535-47bb-0310-9956-ffa450edef68