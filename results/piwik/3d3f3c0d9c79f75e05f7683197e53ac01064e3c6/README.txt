commit 3d3f3c0d9c79f75e05f7683197e53ac01064e3c6
Author: mattab <matthieu.aubry@gmail.com>
Date:   Wed Mar 12 21:24:24 2014 +1300

    Fixes #4768 Implement performance improvement for period=range: do not archive sub-tables (only the parent table).
    The sub-tables will be archived only when idSubtable is found, or flat=1, or expanded=1