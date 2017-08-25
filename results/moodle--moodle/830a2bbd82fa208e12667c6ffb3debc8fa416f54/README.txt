commit 830a2bbd82fa208e12667c6ffb3debc8fa416f54
Author: defacer <defacer>
Date:   Sat Mar 26 18:43:58 2005 +0000

    Yay! Major DST support progress!

    1. calculate_user_dst_table() works correctly and offers many features:

    Can create or extend on-the-fly a table of pre-computed timestamps on which
    DST changes happen, keeps track of these changes and the current computed year
    range in $USER->dstoffsets and $USER->dstrange. Initially the computation is
    done in a current year +/- 3 years range, to keep the amount of serialized
    data small and to make traversals of $USER->dstoffsets faster.

    2. dst_offset_on() works correctly, and can dynamically instruct the above
    function to extend the pre-calculated region on demand (i.e., when it's called
    for a timestamp which falls outside said region).

    3. Some other improvements.



    I have made a few preliminary tests on my dev machine and It Works(tm!) :D