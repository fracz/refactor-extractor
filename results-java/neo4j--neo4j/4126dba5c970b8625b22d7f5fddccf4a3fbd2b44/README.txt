commit 4126dba5c970b8625b22d7f5fddccf4a3fbd2b44
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Wed Aug 23 11:22:27 2017 +0200

    Replication throttling

    Puts a limit on how much data can be in the replication phase concurrently
    and at the same time improves the messaging stack so that the send calls
    have the option to block. This allows better control over resends which
    previously could be attempted even before the queued messages had
    hit the network. Because the channel is shared with time critical
    messages it also allows a stable behaviour under situations where alot
    of concurrently replicating activity is ongoing, which with a limit
    in place will allow the time critical messages to mesh better into the flow.

    This is a medium-term solution which will be improved.