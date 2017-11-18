commit 164ca032b609c9590b41dfd83c13317612e43b2f
Author: Robert Greenwalt <rgreenwalt@google.com>
Date:   Thu Jan 27 16:35:18 2011 -0800

    Fix data network type notifications.

    These trackers have two copies of the network type: newSS and newNetworkType.  I think thats wrong,
    but this was the smaller change on code that will hopefully be refactored soon.

    On radio_off we were making a new, empty newSS but not clearing newNetworkType so it
    still thought we were on 3G and when we reconnect and get 3G state changes new==old and we don't
    send the update.  In this fix I reset newNetworkType every time we apply it to networkType.

    bug:3389886
    Change-Id: I294f34259dc6c6f8f445bf2cb5466c8be747e25c