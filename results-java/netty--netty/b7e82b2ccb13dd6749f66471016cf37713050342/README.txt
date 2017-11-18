commit b7e82b2ccb13dd6749f66471016cf37713050342
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Fri Nov 21 21:13:41 2014 +0100

    Efficiently handle writing ( wrap(...) ) of CompositeByteBuf when using SslHandler

    Motivation:

    SslHandler.wrap(...) does a poor job when handling CompositeByteBuf as it always call ByteBuf.nioBuffer() which will do a memory copy when a CompositeByteBuf is used that is backed by multiple ByteBuf.

    Modifications:

    - Use SslEngine.wrap(ByteBuffer[]...) to allow wrap CompositeByteBuf in an efficient manner
    - Reduce object allocation in unwrapNonAppData(...)

    Result:

    Performance improvement when a CompositeByteBuf is written and the SslHandler is in the ChannelPipeline.