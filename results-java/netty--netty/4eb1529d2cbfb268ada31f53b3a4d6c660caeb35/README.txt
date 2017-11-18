commit 4eb1529d2cbfb268ada31f53b3a4d6c660caeb35
Author: Matthias Einwag <matthias.einwag@live.com>
Date:   Thu Oct 2 00:25:24 2014 +0200

    Improve WebSocket performance

    Motivation:

    Websocket performance is to a large account determined through the masking
    and unmasking of frames. The current behavior of this in Netty can be
    improved.

    Modifications:

    Perform the XOR operation not bytewise but in int blocks as long as
    possible. This reduces the number of necessary operations by 4. Also don't
    read the writerIndex in each iteration.
    Added a unit test for websocket decoding and encoding for verifiation.

    Result:

    A large performance gain (up to 50%) in websocket throughput.