commit dc13b68632957faf6e2e3a57a0e40c11cce45366
Author: Trustin Lee <t@motd.kr>
Date:   Thu May 16 19:32:39 2013 +0900

    Make sure ChannelHandler.handlerRemoved() is always invoked

    - Fixes #1366: No elegant way to free non-in/outbound buffers held by a handler
    - handlerRemoved() is now also invoked when a channel is deregistered, as well as when a handler is removed from a pipeline.
    - A little bit of clean-up for readability
    - Fix a bug in forwardBufferContentAndRemove() where the handler buffers are not freed (mainly because we were relying on channel.isRegistered() to determine if the handler has been removed from inside the handler.
    - ChunkedWriteHandler.handlerRemoved() is unnecessary anymore because ChannelPipeline now always forwards the content of the buffer.