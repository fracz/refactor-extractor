commit ab313f8443c7dade78b7ae53d18dba002a144425
Author: Lorenzo Colitti <lorenzo@google.com>
Date:   Thu Nov 26 16:55:15 2015 +0900

    Don't show "Connected, no Internet" any longer than necessary.

    Currently, both the wifi detail view in Quick Settings and the
    wifi settings screen show "connected, no internet" for a few
    seconds after a network connects. This makes it seem that
    network validation is slow, even though usually it only takes
    tens or hundreds of milliseconds.

    Fix this by updating the access point list when we receive an
    onCapabilitiesChanged NetworkCallback for the current wifi
    connection.

    This also allows us to stop listening to the RSSI_CHANGED_ACTION
    broadcast, because:

    1. WifiStateMachine only ever sends out that broadcast just after
       calling updateCapabilities, which causes an
       onCapabilitiesChanged callback to be received.
    2. We don't use any of the extras in the RSSI_CHANGED_ACTION
       broadcast, only call updateNetworkInfo, which we do on
       every onCapabilitiesChanged callback anyway.

    While I'm at it, move a variable assignment around to improve
    clarity.

    Change-Id: I57d3a13754ba4d8c6d58c566713d3f07d0ed2476