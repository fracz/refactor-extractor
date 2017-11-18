commit 6ac9b17ddd306bcf3dceeb62134a7e38103ac2ac
Author: Norman Maurer <nmaurer@redhat.com>
Date:   Thu Mar 7 10:57:27 2013 +0100

    Make WebSocket codec also work when HttpClientCodec and HttpServerCodec is used.

    Also refactor the handshakers to share more code and make it easier to implement a new one and less error-prone