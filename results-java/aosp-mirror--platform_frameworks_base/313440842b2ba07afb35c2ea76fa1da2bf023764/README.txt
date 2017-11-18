commit 313440842b2ba07afb35c2ea76fa1da2bf023764
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Feb 19 19:22:59 2013 -0800

    Overscan improvement: all apps extend to overscan area.

    Introduce another new rectangle in window layout, the
    restricted overscan region.  This is for windows that can
    be in the restricted region (that is, full screen but with
    hard restrictions like the nav bar) but whose content can
    be adjusted for the overscan constraints.

    In other words, normal application windows now always extend
    into the overscan region, and we push their content inside
    of it.

    Change-Id: Ibccf3d7f144912d49de3fc497c1ec2e8e0b7f714