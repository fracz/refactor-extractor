commit f7b6e27ea2558ddfc97547178408841c5d51c5b6
Author: Michael Hamann <michael@content-space.de>
Date:   Wed May 25 12:15:04 2011 +0200

    Remove the security token from the ajax draft/lock calls again

    The security token here doesn't improve the security as the other
    requests that allow you to do the same thing aren't protected and I
    don't see why locking or draft creation should be subject of XSRF
    attacks.