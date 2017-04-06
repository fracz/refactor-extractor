commit 3719f75d3b70e2510f9eb1de1c1bbfa2ca39f302
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Thu Dec 22 16:14:33 2016 -0500

    Minor refactoring + polish

    - RxNettyWebSocketSession filters out WebSocketCloseFrame again
    - add before/afterHandshake helper methods in WebSocketClientSupport
    - log request headers on server and response headers on client
    - polish 400 request handling in HandshakeWebSocketService