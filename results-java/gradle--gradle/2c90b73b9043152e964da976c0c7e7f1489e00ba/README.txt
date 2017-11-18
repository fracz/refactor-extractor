commit 2c90b73b9043152e964da976c0c7e7f1489e00ba
Author: Lóránt Pintér <lorant@gradle.com>
Date:   Tue Aug 30 21:57:10 2016 +0200

    Short circuit when we have no snapshots to compare

    This should improve performance when there are not files for a property.

    +review REVIEW-6170