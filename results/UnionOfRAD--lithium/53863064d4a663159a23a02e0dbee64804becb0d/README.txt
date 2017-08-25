commit 53863064d4a663159a23a02e0dbee64804becb0d
Author: Michael Nitschinger <michael@nitschinger.at>
Date:   Fri Jul 29 21:47:24 2011 +0200

    refactoring internet connection check into \test\Unit. It is now possible to check for a successful internet uplink in a central place. Also modifying the integration SocketTest to use the new method.