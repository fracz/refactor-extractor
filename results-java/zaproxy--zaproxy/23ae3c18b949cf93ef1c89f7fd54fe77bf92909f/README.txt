commit 23ae3c18b949cf93ef1c89f7fd54fe77bf92909f
Author: robert.koch@loggia.at <robert.koch@loggia.at>
Date:   Wed Jul 18 21:24:12 2012 +0000

    added database support to WebSockets
     - save messages in WebSocketStorage class, that is added as WebSocketObserver to each channel
     - read messages for GUI from database (use paged table model as base)
     - introduced new method onStateChange() in WebSocketObserver
     - use of another DAO for channels
     - refactored ComboBox for channels to use new DAO