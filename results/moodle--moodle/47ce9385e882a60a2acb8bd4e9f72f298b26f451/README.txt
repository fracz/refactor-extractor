commit 47ce9385e882a60a2acb8bd4e9f72f298b26f451
Author: David Mudrak <david@moodle.com>
Date:   Thu Jan 13 14:20:59 2011 +0100

    MDL-25526 Workshop: fixed random allocation of reviews

    The patch wraps that foreach ($circles as $circleid => $circle)
    loop by yet another one for() loop. Reviews are allocated iteratively
    now. During the first iteration, we try to make sure that at least one
    circle link exists. During the second iteration, we try to allocate two,
    etc. Circles are shuffled at the beginning of each iteration.
    This is supposed to improve the randomness of the allocation.

    The patch also fixes shuffle_assoc() implementation. The previous
    implementation actually did not work at all. Also, that removed called
    to shuffle_assoc() was redundant here.