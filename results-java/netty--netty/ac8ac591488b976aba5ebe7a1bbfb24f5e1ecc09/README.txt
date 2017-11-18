commit ac8ac591488b976aba5ebe7a1bbfb24f5e1ecc09
Author: Brendt Lucas <brendt@idnet.com>
Date:   Tue Jul 8 18:31:37 2014 +0100

    [#2642] CompositeByteBuf.deallocate memory/GC improvement

    Motivation:

    CompositeByteBuf.deallocate generates unnecessary GC pressure when using the 'foreach' loop, as a 'foreach' loop creates an iterator when looping.

    Modification:

    Convert 'foreach' loop into regular 'for' loop.

    Result:

    Less GC pressure (and possibly more throughput) as the 'for' loop does not create an iterator