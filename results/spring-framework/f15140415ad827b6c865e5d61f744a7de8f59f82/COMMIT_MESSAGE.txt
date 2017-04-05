commit f15140415ad827b6c865e5d61f744a7de8f59f82
Author: Gary Russell <grussell@pivotal.io>
Date:   Wed Aug 5 12:35:55 2015 -0400

    Properly detect available port on localhost in SocketUtils

    SocketUtils is used to find available ports on localhost; however,
    prior to this commit, SocketUtils incorrectly reported a port as
    available on localhost if another process was already bound to
    localhost on the given port but not to other network interfaces. In
    other words, SocketUtils determined that a given port was available for
    some interface though not necessarily for the loopback interface.

    This commit addresses this issue by refactoring SocketUtils so that it
    tests the loopback interface to ensure that the port is actually
    available for localhost.

    Issue: SPR-13321