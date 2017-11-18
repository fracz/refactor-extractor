commit 5834c4f442268e16fda48db0ae58782dc9e305c0
Author: Adam Murdoch <adam.murdoch@gradleware.com>
Date:   Wed Dec 19 15:42:19 2012 +1100

    MessageHub improvements:
    - Fire disconnect events when finished with a connection.
    - When a HubStateListener is added, fire connect events for any connections already established.
    - Include a simple handshake when tearing down a connection.
    - Some better exception handling at the top of each thread.
    - Don't keep queuing messages for handlers or connections that are broken or have finished.