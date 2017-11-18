commit b8d26d1da3f203033e1f5814d9b72582376cb0b5
Author: Cristian <me@cristian.io>
Date:   Sun Feb 15 21:36:43 2015 -0800

    Avoid unnecessary call to ByteBuf.isReadable() from ByteToMessageDecoder

    Motivation:

    This will avoid one unncessary method invokation which will slightly improve performance.

    Modifications:

    Instead of calling isReadable we just check for the value of readableBytes()

    Result:

    Nothing functionally speaking change.