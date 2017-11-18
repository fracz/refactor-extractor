commit 8600335e8e24b3b5be9ae29b7a9276d4b724424c
Author: Lisa Luo <luoser@users.noreply.github.com>
Date:   Mon Jan 9 17:21:45 2017 -0500

    Pass categories from DiscoveryActivity to Fragment (#44)

    * Modernize DiscoveryVM

    * Modernize DiscoveryFragmentVM

    * Cleanup params logic in DiscoveryVM; refactor output to add Categories

    * Remove redundant Categories api request from fragment; add/update tests

    Pass along the root categories from the activity instead, via the pagerAdapter.

    * Add rootCategories to factory

    * need this.

    * Fix up annotations

    * Revert "Fix up annotations"

    This reverts commit 3db74040f5f61ddd61b8826e55252446fd14caeb.

    * Fix up annotations without violating LeftCurly check