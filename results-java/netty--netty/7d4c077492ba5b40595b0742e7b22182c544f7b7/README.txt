commit 7d4c077492ba5b40595b0742e7b22182c544f7b7
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Fri Oct 9 21:03:03 2015 +0200

    Add *UnsafeHeapByteBuf for improve performance on systems with sun.misc.Unsafe

    Motivation:

    sun.misc.Unsafe allows us to handle heap ByteBuf in a more efficient matter. We should use special ByteBuf implementation when sun.misc.Unsafe can be used to increase performance.

    Modifications:

    - Add PooledUnsafeHeapByteBuf and UnpooledUnsafeHeapByteBuf that are used when sun.misc.Unsafe is ready to use.
    - Add UnsafeHeapSwappedByteBuf

    Result:

    Better performance when using heap buffers and sun.misc.Unsafe is ready to use.