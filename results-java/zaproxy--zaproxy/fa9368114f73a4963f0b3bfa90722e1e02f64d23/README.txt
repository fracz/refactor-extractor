commit fa9368114f73a4963f0b3bfa90722e1e02f64d23
Author: robert.koch@loggia.at <robert.koch@loggia.at>
Date:   Tue Jul 10 11:50:17 2012 +0000

    exchanged "speech bubble" icon for WebSockets with "plug" (either connected or disconnected depending on channel status)
    show new icons in SiteMap, Request/Response/Break tab, Channel Selector in WebSockets tab plus channel selector in "Add Breakpoint" popup
    refactored WebSocketPanel and its handling of the ComboBox (for the channel select)
    new functionality: show WebSocket handshake message on new "handshake" button in WebSockets tab