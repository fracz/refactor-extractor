commit f96ff3f9306db0ea87f66ff298c21db1bf1cc86f
Author: Lóránt Pintér <lorant@gradle.com>
Date:   Tue Aug 16 13:51:28 2016 +0200

    Refactor code for better readability

    Each file collection snapshot is in its own top-level class now, with the respective serializer declared as an inner class.

    +review REVIEW-6141