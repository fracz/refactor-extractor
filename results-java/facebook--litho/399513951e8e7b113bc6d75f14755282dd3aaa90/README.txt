commit 399513951e8e7b113bc6d75f14755282dd3aaa90
Author: Rick Ratmansky <rratmansky@fb.com>
Date:   Fri Sep 22 15:38:57 2017 -0700

    Backing out Diff D5880914 which inadvertantly broke the snap to next for friending.

    Summary: With the original diff in place, clicking add friend does not move you to the next person in the list.  This feature generally improves friending by around 10%.  It's very important it does not break, which is why we have to backout out the original diff.

    Differential Revision: D5895292

    fbshipit-source-id: bc4e1eb1214d6ae5e31b77739af8cba25e884c81