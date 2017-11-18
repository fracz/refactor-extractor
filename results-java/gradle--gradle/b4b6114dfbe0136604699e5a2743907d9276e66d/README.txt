commit b4b6114dfbe0136604699e5a2743907d9276e66d
Author: Paul Merlin <paul@gradle.com>
Date:   Fri Aug 19 10:59:26 2016 +0200

    Cache native dependent binaries resolution strategy state

    Cache scope is per project, per build.
    There still is room for improvements but this change drastically lower
    resolution times already.

    +review REVIEW-6179