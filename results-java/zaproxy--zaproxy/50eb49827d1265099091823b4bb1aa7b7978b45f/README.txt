commit 50eb49827d1265099091823b4bb1aa7b7978b45f
Author: robert.koch@loggia.at <robert.koch@loggia.at>
Date:   Wed May 30 12:49:34 2012 +0000

    directly re-use input stream of HttpClient library as first WebSocket frames may be in same TCP packet as the handshake response (as tested by Autobahn)
    improved logging
    renamed flagsByte to frameHeader in WebSocketsProxy