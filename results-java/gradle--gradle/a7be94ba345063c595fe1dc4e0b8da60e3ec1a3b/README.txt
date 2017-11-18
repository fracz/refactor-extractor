commit a7be94ba345063c595fe1dc4e0b8da60e3ec1a3b
Author: Lóránt Pintér <lorant@gradle.com>
Date:   Wed Aug 3 19:10:53 2016 +0200

    Introduce HTTP task output cache backend

    This adds a nice reference implementation for task output cache backends, and also serves as a versatile backend protocol.

    Also refactored the cache protocol itself to be simpler and more streaming-oriented to support networked backends better.

    +review REVIEW-6146