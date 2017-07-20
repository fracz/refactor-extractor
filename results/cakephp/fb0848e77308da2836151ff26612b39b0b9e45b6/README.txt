commit fb0848e77308da2836151ff26612b39b0b9e45b6
Author: Mark Story <mark@mark-story.com>
Date:   Tue Dec 1 22:34:16 2015 -0500

    Make RelativeTimeFormatter static.

    This will result in fewer object constructions and hopefully improved
    performance.