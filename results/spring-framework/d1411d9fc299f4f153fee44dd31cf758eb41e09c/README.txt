commit d1411d9fc299f4f153fee44dd31cf758eb41e09c
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Fri Dec 9 20:38:56 2016 +0200

    Simple refactorings in AbstractListenerWebSocketSessionSupport

    Dropped "Support" from the name since it not only provides support
    methods but actually implements WebSocketSession.

    Renamed inner classes:
    WebSocketMessagePublisher -> WebSocketReceivePublisher
    WebSocketMessageProcessor -> WebSocketSendProcessor

    Add protected getter for sendProcessor.

    Reduce scoping:
    WebSocketReceivePublisher -> private
    WebSocketSendProcessor -> protected
    WebSocketSendProcessor#setReady -> public (class is still protected)

    A few more method name alignments and Javadoc updates.

    Issue: SPR-14527