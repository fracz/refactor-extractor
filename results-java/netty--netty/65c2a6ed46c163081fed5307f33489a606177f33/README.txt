commit 65c2a6ed46c163081fed5307f33489a606177f33
Author: Trustin Lee <t@motd.kr>
Date:   Mon Jul 8 13:32:33 2013 +0900

    Make ByteBuf an abstract class rather than an interface

    - 5% improvement in throughput (HelloWorldServer example)
    - Made CompositeByteBuf a concrete class (renamed from DefaultCompositeByteBuf) because there's no multiple inheritance in Java

    Fixes #1536