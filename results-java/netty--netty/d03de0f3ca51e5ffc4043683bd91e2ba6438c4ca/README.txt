commit d03de0f3ca51e5ffc4043683bd91e2ba6438c4ca
Author: Trustin Lee <trustin@gmail.com>
Date:   Wed Aug 29 21:49:39 2012 +0900

    [#107] Add support for closing either input or output part of a channel

    - Add ChannelOption.ALLOW_HALF_CLOSURE
      - If true, ChannelInputShutdownEvent is fired via userEventTriggered()
        when the remote peer shuts down its output, and the connection is
        not closed until a user calls close() explicitly.
      - If false, the connection is closed immediately as it did before.
    - Add SocketChannel.isInputShutdown()
    - Add & improve test cases related with half-closed sockets