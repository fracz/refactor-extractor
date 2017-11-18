commit 1093dc11740eb55980ef800a54a5128e8bc39cf3
Author: Julien Viet <julien@julienviet.com>
Date:   Fri Jul 21 11:43:30 2017 +0200

    A few improvements in websocket rejection: the ServerWebSocket#reject() is overloaded to provide a specific status code, the reject() method is documented that it will send 502 by default and not 404, the client can be aware of the rejection status with the new WebsocketRejectedException. fixes #1996