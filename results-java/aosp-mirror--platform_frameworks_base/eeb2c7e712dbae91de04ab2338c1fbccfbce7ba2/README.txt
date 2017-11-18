commit eeb2c7e712dbae91de04ab2338c1fbccfbce7ba2
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Sep 23 16:45:39 2014 -0700

    Work on issue #17628623: Need to update default preferred activities for YouTube

    Improve the warning logs when setting up preferred activities
    to help identify when there are issues and what they are.  Also
    improve the algorithm a little to still apply permissions when
    resetting them and there are additional third party apps, as long
    as the additional app is something like another browser and the
    preferred activity being set is more specific (has a better match).

    And add an example of using manifest-based preferred activities
    in to ActivityTest -- and yes it DOES work! :p

    Change-Id: I1ff39e03a5df6526206e0c3882085396b355d814