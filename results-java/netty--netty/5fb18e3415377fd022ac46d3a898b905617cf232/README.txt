commit 5fb18e3415377fd022ac46d3a898b905617cf232
Author: Scott Mitchell <scott_mitchell@apple.com>
Date:   Thu Jan 28 13:07:56 2016 -0800

    InboundHttp2ToHttpAdapter leak and logic improvements

    Motivation:
    In HttpConversionUtil's toHttpRequest and toHttpResponse methods can
    allocate FullHttpMessage objects, and if an exeception is thrown during
    the header conversion then this object will not be released. If a
    FullHttpMessage is not fired up the pipeline, and the stream is closed
    then we remove from the map, but do not release the object. This leads
    to a ByteBuf leak. Some of the logic related to stream lifetime management
    and FullHttpMessage also predates the RFC being finalized and is not correct.

    Modifications:
    - Fix leaks in HttpConversionUtil
    - Ensure the objects are released when they are removed from the map.
    - Correct logic and unit tests where they are found to be incorrect.

    Result:
    Fixes https://github.com/netty/netty/issues/4780
    Fixes https://github.com/netty/netty/issues/3619