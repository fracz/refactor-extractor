commit a11e6e3ff902a72c1b4eb1e9eec55479282e6953
Author: robert.koch@loggia.at <robert.koch@loggia.at>
Date:   Wed Jul 25 09:52:58 2012 +0000

    added WebSocket options
     - button in WebSocket tab
     - 3 checkboxes in options dialog (forward all, break on all, break on ping/pong)
     - applied options
     - added help pages (WebSocket tab + WebSocket options)

    refactored ExtensionWebSocket:
     - clearer UI split (UI related code is private-only)
     - add options panel in hook() method
     - use custom BreakpointMessageHandler (to enforce new options)

    fixed some minor issues (missing license headers, NullPointerException on session save)