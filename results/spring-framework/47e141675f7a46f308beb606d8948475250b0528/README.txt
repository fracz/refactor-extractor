commit 47e141675f7a46f308beb606d8948475250b0528
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Mon Dec 19 18:07:22 2016 -0500

    Minor refactoring + polish reactive WebSocket support

    Rename classes not specific to Tomcat:
    TomcatWebSocketSession -> StandardWebSocketSession
    TomcatWebSocketHandlerAdapter -> StandardWebSocketHandlerAdapter

    WebSocketSessionSupport is renamed to AbstractWebSocketSession since it
    actually is a WebSocketSession and pre-implements a number of methods.

    ServerEndpointRegistration is now package private (mainly for use in
    upgrade strategies) and renamed to DefaultServerEndpointConfig.