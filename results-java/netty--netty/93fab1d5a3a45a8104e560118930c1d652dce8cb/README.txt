commit 93fab1d5a3a45a8104e560118930c1d652dce8cb
Author: Norman Maurer <nmaurer@redhat.com>
Date:   Wed Apr 30 08:04:35 2014 +0200

    Remove ContinuationWebSocketFrame.aggregatedText()

    Motivation:
    Before we aggregated the full text in the WebSocket08FrameDecoder just to fill in the ContinuationWebSocketFrame.aggregatedText(). The problem was that there was no upper-limit and so it would be possible to see an OOME if the remote peer sends a TextWebSocketFrame + a never ending stream of ContinuationWebSocketFrames. Furthermore the aggregation does not really belong in the WebSocket08FrameDecoder, as we provide an extra ChannelHandler for this anyway (WebSocketFrameAggregator).

    Modification:
    Remove the ContinuationWebSocketFrame.aggregatedText() method and corresponding constructor. Also refactored WebSocket08FrameDecoder a bit to me more efficient which is now possible as we not need to aggregate here.

    Result:
    No more risk of OOME because of frames.