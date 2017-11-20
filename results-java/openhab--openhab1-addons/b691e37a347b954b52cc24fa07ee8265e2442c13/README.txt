commit b691e37a347b954b52cc24fa07ee8265e2442c13
Author: peuter <tbraeutigam@gmail.com>
Date:   Mon Jan 27 19:34:18 2014 +0100

    - removed unneccessary PollingDelayFilter
    - improved implementation of the BroadcasterCache
    - Bugfix: during a reconnect, the Broadcaster is emtpy and no messages
    where broadcasted and therefore these messages could not be cached and
    were lost. Now all status updates are broadcasted. The Atmosphere
    framework decides wether the update is directly broadcasted (when there
    are clients available) or it is cached (when there are no clients
    available) or nothing happens (when the broadcaster got destroyed
    because it has been idle for at least 5 minutes)