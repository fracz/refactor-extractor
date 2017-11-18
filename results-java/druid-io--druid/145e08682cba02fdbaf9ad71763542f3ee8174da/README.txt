commit 145e08682cba02fdbaf9ad71763542f3ee8174da
Author: cheddar <cheddar@metamarkets.com>
Date:   Tue Aug 13 17:01:24 2013 -0700

    1) Add check in ServerManagerTest to make sure that the Segment has been "checked out" before the factory ever sees it.
    2) Some code readability changes to ReferenceCountingSegment