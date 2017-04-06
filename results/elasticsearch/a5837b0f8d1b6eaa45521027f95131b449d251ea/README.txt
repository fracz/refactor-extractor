commit a5837b0f8d1b6eaa45521027f95131b449d251ea
Author: Simon Willnauer <simonw@apache.org>
Date:   Sun Jun 2 10:04:47 2013 +0200

    Stableize SearchStatsTest after search refactoring

    SearchStatsTest depends on a given set of nodes and shards. The test
    needed to be adjusted to reflect a possibly random number of nodes.