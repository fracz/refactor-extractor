commit 6a6e1bed55271bfd10fdb29cb7ab42c45aaec888
Author: Jason Tedor <jason@tedor.me>
Date:   Mon Nov 7 16:35:39 2016 -0500

    Remove JVMCheck

    This commit removes JVMCheck. Previously there were three checks in this
    class:
     - check for super word bug in JDK 7
     - check for G1GC bugs in JDK 8
     - check for broken IBM JDKs

    The first check is obsolete since we require JDK 8 now. The second check
    is refactored into a bootstrap check. The third check is removed since
    we do not even support the IBM JDK.

    Relates #21389