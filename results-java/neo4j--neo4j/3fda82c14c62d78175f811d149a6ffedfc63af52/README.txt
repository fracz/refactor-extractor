commit 3fda82c14c62d78175f811d149a6ffedfc63af52
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Wed Jul 6 16:25:37 2016 +0200

    core-edge: better membership storage

    The RAFT membership storage is now in line with the intended design
    and only stores a single committed state and potentially an additional
    appended state. This simplifies the implementation considerably and
    improves performance since the dependence on the RAFT log as the
    underlying storage is removed. This mismatch was forcing extremely
    bad usage patterns of the RAFT log from the membership manager,
    causing unnecessary linear scans.