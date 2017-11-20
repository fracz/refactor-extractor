commit b7eebde95ad646a83bf5b8bbf869f1069d3377ac
Author: robert.koch@loggia.at <robert.koch@loggia.at>
Date:   Sun Jul 8 22:19:15 2012 +0000

    refactored common parts of BreadAddDialog & BreakEditDialog into one abstract class WebSocketBreakDialog
    added button for adding custom breakpoints to WebSocket panel
    allow not only opcode selection for custom breakpoints, but also channel and payload pattern