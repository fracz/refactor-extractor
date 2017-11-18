commit 693322ca1a6a419a8d27378e65829600b00badd4
Author: Max Sumrall <max.sumrall@neotechnology.com>
Date:   Tue May 31 16:37:55 2016 +0200

    Improved test for catching up rejoined follower.

    The improved test explicitly checks the logs that have accumulated
    and also the logs which have been pruned and coordinates the
    shutdown and startup of the follower accordingly.