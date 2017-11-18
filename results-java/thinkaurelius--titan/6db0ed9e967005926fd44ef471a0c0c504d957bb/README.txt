commit 6db0ed9e967005926fd44ef471a0c0c504d957bb
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Wed May 29 19:48:43 2013 -0400

    Cassandra-Thrift refactoring

    This commit subsumes the useful changes in the thrift-refactoring
    branch.  It's not a merge commit because titan03 and
    thrift-refactoring had diverged enough to cause conflicts that seemed
    to take longer to resolve than reproducing the changes on titan03.

    This commit also updates to Apache commons-pool 1.6.  This
    commons-pool version includes support for generics, obviating a bunch
    of generics code formerly in
    titan-cassandra/src/main/java/.../thrift/thriftpool.