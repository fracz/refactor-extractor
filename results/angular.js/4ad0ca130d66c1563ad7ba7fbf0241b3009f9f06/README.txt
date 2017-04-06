commit 4ad0ca130d66c1563ad7ba7fbf0241b3009f9f06
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Wed Oct 7 12:27:48 2015 +0100

    refactor($compile): check removeWatches before calling

    Previously we assigned `noop` if there was no function but there is no
    performance advantage in doing this since the check would have to happen
    either at assignment time or at call time.

    Removing this use of `noop` makes the code clearer, IMO :-)

    Closes #12528