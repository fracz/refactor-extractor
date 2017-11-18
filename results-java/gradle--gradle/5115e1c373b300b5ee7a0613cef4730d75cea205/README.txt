commit 5115e1c373b300b5ee7a0613cef4730d75cea205
Author: Stefan Oehme <stefan@gradle.com>
Date:   Thu Aug 4 19:50:16 2016 +0200

    Start performance graphs at 0

    This smoothes out tiny differences in runtime,
    making the graphs easier to read at a glance.

    At the same time it will help us see the user's
    perspective. What matters to the user is the relative
    improvement, not the absolute one. A 0.5s difference
    can be highly significant for a build that used to take 1s,
    but doesn't matter much if the base line is 30s.

    The graphs can now be zoomed both on the x and y axis to focus
    on a specific detail.