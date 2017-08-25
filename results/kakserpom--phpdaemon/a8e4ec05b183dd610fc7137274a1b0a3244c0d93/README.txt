commit a8e4ec05b183dd610fc7137274a1b0a3244c0d93
Author: Erika31 <r.i.k@free.fr>
Date:   Tue Oct 18 13:25:26 2011 +0200

    Improved "AsyncServer::checkAccept" to pass the same arguments as
    "AsyncServer::onAcceptEvent"
    Created a new "genericWebSocketServer", able to deal with old (aka
    IETF hixie-76) and new (aka IETF hybi-10) websocket protocol, and being
    scalable with future protocol improvements
    Created the "ExampleGenericWebSocket" class as...  ...an example ;-)
    Does not deal yet with BINARY frame type ;-(