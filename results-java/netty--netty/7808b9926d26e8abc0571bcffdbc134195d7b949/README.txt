commit 7808b9926d26e8abc0571bcffdbc134195d7b949
Author: Jeff Pinner <jpinner@twitter.com>
Date:   Sun Apr 13 13:55:29 2014 -0700

    SPDY: refactor frame codec implementation

    Motivation:

    Currently, the SPDY frame encoding and decoding code is based upon
    the ChannelHandler abstraction. This requires maintaining multiple
    versions for 3.x and 4.x (and possibly 5.x moving forward).

    Modifications:

    The SPDY frame encoding and decoding code is separated from the
    ChannelHandler and SpdyFrame abstractions. Also test coverage is
    improved.

    Result:

    SpdyFrameCodec now implements the ChannelHandler abstraction and is
    responsible for creating and handling SpdyFrame objects.