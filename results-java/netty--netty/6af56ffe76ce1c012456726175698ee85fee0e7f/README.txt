commit 6af56ffe76ce1c012456726175698ee85fee0e7f
Author: Scott Mitchell <scott_mitchell@apple.com>
Date:   Thu Jun 23 10:38:27 2016 -0700

    HPACK Encoder headerFields improvements

    Motivation:
    HPACK Encoder has a data structure which is similar to a previous version of DefaultHeaders. Some of the same improvements can be made.

    Motivation:
    - Enforce the restriction that the Encoder's headerFields length must be a power of two so we can use masking instead of modulo
    - Use AsciiString.hashCode which already has optimizations instead of having yet another hash code algorithm in Encoder

    Result:
    Fixes https://github.com/netty/netty/issues/5357