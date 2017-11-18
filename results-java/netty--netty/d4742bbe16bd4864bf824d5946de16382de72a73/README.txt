commit d4742bbe16bd4864bf824d5946de16382de72a73
Author: Trustin Lee <trustin@gmail.com>
Date:   Wed Feb 6 12:55:42 2013 +0900

    Clean up abstract ChannelHandler impls / Remove ChannelHandlerContext.hasNext*()

    - Rename ChannelHandlerAdapter to ChannelDuplexHandler
    - Add ChannelHandlerAdapter that implements only ChannelHandler
    - Rename CombinedChannelHandler to CombinedChannelDuplexHandler and
      improve runtime validation
    - Remove ChannelInbound/OutboundHandlerAdapter which are not useful
    - Make ChannelOutboundByteHandlerAdapter similar to
      ChannelInboundByteHandlerAdapter
    - Make the tail and head handler of DefaultChannelPipeline accept both
      bytes and messages.  ChannelHandlerContext.hasNext*() were removed
      because they always return true now.
    - Removed various unnecessary null checks.
    - Correct method/field names:
      inboundBufferSuspended -> channelReadSuspended