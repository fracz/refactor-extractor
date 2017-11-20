commit 09198a5278fc92e80de2aea187166cd8adfbb7ef
Author: lvca <l.garulli@gmail.com>
Date:   Mon Aug 12 02:09:15 2013 +0200

    client-server network improvements

    This commit has many improvement on network part:
    - fixed loose of network connection even after much time since
    reconnection
    - changed management of network connections: now is saved in TL also the
    server URL, so on reconnection to another server the session ID is
    reset to -1
    - unreachable servers now are in the tail of server list