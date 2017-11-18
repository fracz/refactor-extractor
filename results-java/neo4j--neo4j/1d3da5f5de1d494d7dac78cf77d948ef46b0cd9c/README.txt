commit 1d3da5f5de1d494d7dac78cf77d948ef46b0cd9c
Author: Pontus Melke <pontusmelke@gmail.com>
Date:   Mon Aug 22 13:32:26 2016 +0200

    Add type back to `db.indexes()`

    The type was added in 3.0 but has disappeared in 3.1 due to a
    merge/refactoring. This PR adds the type back to the output of
    `db.indexes()`.