commit 6615d72db072b02a2dd66620d4ac58b8785350d2
Author: Norman Maurer <nmaurer@redhat.com>
Date:   Fri Apr 11 15:54:31 2014 +0200

    [#2376] Add support for SO_REUSEPORT in native transport

    Motivation:
    In linux kernel 3.9 a new featured named SO_REUSEPORT was introduced which allows to have multiple sockets bind to the same port and so handle the accept() of new connections with multiple threads. This can greatly improve the performance when you not to accept a lot of connections.

    Modifications:
    Implement SO_REUSEPORT via JNI

    Result:
    Be able to use the SO_REUSEPORT feature when using the EpollServerSocketChannel