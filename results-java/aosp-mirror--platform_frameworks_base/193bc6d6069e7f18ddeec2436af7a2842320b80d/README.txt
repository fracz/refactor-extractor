commit 193bc6d6069e7f18ddeec2436af7a2842320b80d
Author: Ben Komalo <benkomalo@google.com>
Date:   Wed Jun 8 16:18:57 2011 -0700

    Open up setKeyManager/setTrustManager.

    The improved keystore will allow clients (Email not the least of them)
    to establish SSL connections using custom client certificates. In order
    to do this properly, the socket factories they use to establish these
    connections need to be able to customize their behavior.

    Change-Id: I6e0fa04dd01bd6481dfdad5a71a63e0371d0ad8c