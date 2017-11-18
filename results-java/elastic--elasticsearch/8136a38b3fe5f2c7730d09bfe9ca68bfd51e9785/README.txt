commit 8136a38b3fe5f2c7730d09bfe9ca68bfd51e9785
Author: Shay Banon <kimchy@gmail.com>
Date:   Tue Apr 22 16:09:57 2014 +0200

    Improved bloom filter hashing
    Make improvements to how bloom filter hashing works based on guava 17 upcoming changes, see more here (https://code.google.com/p/guava-libraries/issues/detail?id=1119)
    In order to do it, introduce a hashing enum, and use the (unused until now) hash type serialization to choose the correct hashing used based on serialized version.
    Also, move to use our own optimized murmur hash for the new hashing logic.