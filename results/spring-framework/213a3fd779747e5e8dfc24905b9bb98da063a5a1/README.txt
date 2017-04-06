commit 213a3fd779747e5e8dfc24905b9bb98da063a5a1
Author: Craig Andrews <candrews@integralblue.com>
Date:   Wed Jan 7 16:53:09 2015 +0100

    Performance improvements in ShallowEtagHeaderFilter

    Prior to this change, the ShallowEtagHeaderFilter would use a
    ResizableByteArrayOutputStream to internally write data and calculate
    the ETag. While that implementation is faster than the regular
    ByteArrayOutputStream (since it has a better strategy for growing the
    internal buffer), a lot of buffer copying/writing still happens.

    This change adds a new FastByteArrayOutputStream implementation that
    internally uses a LinkedList<Byte[]> to store the content. So when
    writing bytes to that OutputStream implementation, new byte[] are
    added to the list when the previous ones are full. This saves most
    of the instantiating/copying operations.

    Note that new methods were added in DigestUtils to allow usage of
    Streams instead of byte[], which is more efficient in our case.

    Fixes #653

    Issue: SPR-12081