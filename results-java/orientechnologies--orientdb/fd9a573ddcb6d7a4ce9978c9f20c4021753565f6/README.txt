commit fd9a573ddcb6d7a4ce9978c9f20c4021753565f6
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Wed Nov 24 23:36:30 2010 +0000

    Huge refactoring to the network client layer to:
    - support asynchronous requests
    - support service messages from server (cluster cfg change + pessimistic tx)
    - protocol version is sent to the client once connected