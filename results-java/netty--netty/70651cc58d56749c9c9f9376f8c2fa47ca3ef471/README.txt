commit 70651cc58d56749c9c9f9376f8c2fa47ca3ef471
Author: Scott Mitchell <scott_mitchell@apple.com>
Date:   Wed Jun 22 10:37:35 2016 -0700

    HpackUtil.equals performance improvement

    Motivation:
    PR #5355 modified interfaces to reduce GC related to the HPACK code. However this came with an anticipated performance regression related to HpackUtil.equals due to AsciiString's increase cost of charAt(..). We should mitigate this performance regression.

    Modifications:
    - Introduce an equals method in PlatformDependent which doesn't leak timing information and use this in HpcakUtil.equals

    Result:
    Fixes https://github.com/netty/netty/issues/5436