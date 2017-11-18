commit 646753c2eb3766352c81e783908ef8ceb2dc4423
Author: Norman Maurer <nmaurer@redhat.com>
Date:   Mon Aug 25 08:47:00 2014 +0200

    Allow to write CompositeByteBuf directly via EpollDatagramChannel. Related to [#2719]

    Motivation:

    On linux it is possible to use the sendMsg(...) system call to write multiple buffers with one system call when using datagram/udp.

    Modifications:

    - Implement the needed changes and make use of sendMsg(...) if possible for max performance
    - Add tests that test sending datagram packets with all kind of different ByteBuf implementations.

    Result:

    Performance improvement when using CompoisteByteBuf and EpollDatagramChannel.