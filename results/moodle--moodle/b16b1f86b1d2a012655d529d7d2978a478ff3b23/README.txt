commit b16b1f86b1d2a012655d529d7d2978a478ff3b23
Author: sam marshall <s.marshall@open.ac.uk>
Date:   Fri Aug 19 14:32:51 2016 +0100

    MDL-55650 Library: s() function - tweak parameters for performance

    The s() function includes a comment saying that parameter options
    should be modified to improve performance once PHP 5.4 is required.
    Since Moodle has required PHP 5.4 for some time, we should probably
    make the change and remove the comment.

    According to my benchmarking, these changes make s() about 7% faster
    and will save a staggering 2ms from a typical course view that calls
    it 8,000 times.