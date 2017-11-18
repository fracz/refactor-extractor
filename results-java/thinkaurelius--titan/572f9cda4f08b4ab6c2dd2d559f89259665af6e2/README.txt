commit 572f9cda4f08b4ab6c2dd2d559f89259665af6e2
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Tue Apr 15 06:30:18 2014 -0400

    Additional HBase refactoring

    Added a module that builds a frankenjar containing titan-hbase-core
    plus a pair of compatibility classes that hide minor API differences
    between HBase 0.96 and 0.94.  These compatibility classes are each
    compiled against their respective HBase version; core is compiled
    against 0.96 by default; they all go in a single jar.

    This imports cleanly in Intellij IDEA and Eclipse.