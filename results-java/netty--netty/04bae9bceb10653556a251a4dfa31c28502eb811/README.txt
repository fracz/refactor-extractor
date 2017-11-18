commit 04bae9bceb10653556a251a4dfa31c28502eb811
Author: Trustin Lee <trustin@gmail.com>
Date:   Thu Jan 10 18:27:16 2013 +0900

    Use sun.misc.Unsafe to access a direct ByteBuffer

    - Add PooledUnsafeDirectByteBuf, a variant of PooledDirectByteBuf, which
      accesses its underlying direct ByteBuffer using sun.misc.Unsafe.
    - To decouple Netty from sun.misc.*, sun.misc.Unsafe is accessed via
      PlatformDependent.
    - This change solely introduces about 8+% improvement in direct memory
      access according to the tests conducted as described in #918