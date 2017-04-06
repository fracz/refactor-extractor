commit 68307aa9f3636bdccd1f1ca90f1d0fd640a019a5
Author: Robert Muir <rmuir@apache.org>
Date:   Mon Aug 17 15:37:07 2015 -0400

    Fix network binding for ipv4/ipv6

    When elasticsearch is configured by interface (or default: loopback interfaces),
    bind to all addresses on the interface rather than an arbitrary one.

    If the publish address is not specified, default it from the bound addresses
    based on the following sort ordering:

    * ipv4/ipv6 (java.net.preferIPv4Stack, defaults to true)
    * ordinary addresses
    * site-local addresses
    * link local addresses
    * loopback addresses

    One one address is published, and multicast is still always over ipv4: these
    need to be future improvements.

    Closes #12906
    Closes #12915

    Squashed commit of the following:

    commit 7e60833312f329a5749f9a256b9c1331a956d98f
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 14:45:33 2015 -0400

        fix java 7 compilation oops

    commit c7b9f3a42058beb061b05c6dd67fd91477fd258a
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 14:24:16 2015 -0400

        Cleanup/fix logic around custom resolvers

    commit bd7065f1936e14a29c9eb8fe4ecab0ce512ac08e
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 13:29:42 2015 -0400

        Add some unit tests for utility methods

    commit 0faf71cb0ee9a45462d58af3d1bf214e8a79347c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 12:11:48 2015 -0400

        localhost all the way down

    commit e198bb2bc0d1673288b96e07e6e6ad842179978c
    Merge: b55d092 b93a75f
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 12:05:02 2015 -0400

        Merge branch 'master' into network_cleanup

    commit b55d092811d7832bae579c5586e171e9cc1ebe9d
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 12:03:03 2015 -0400

        fix docs, fix another bug in multicast (publish host = bad here!)

    commit 88c462eb302b30a82585f95413927a5cbb7d54c4
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 11:50:49 2015 -0400

        remove nocommit

    commit 89547d7b10d68b23d7f24362e1f4782f5e1ca03c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 11:49:35 2015 -0400

        fix http too

    commit 9b9413aca8a3f6397b5031831f910791b685e5be
    Author: Robert Muir <rmuir@apache.org>
    Date:   Mon Aug 17 11:06:02 2015 -0400

        Fix transport / interface code

        Next up: multicast and then http