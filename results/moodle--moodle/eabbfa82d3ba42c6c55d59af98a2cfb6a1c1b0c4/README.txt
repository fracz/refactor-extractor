commit eabbfa82d3ba42c6c55d59af98a2cfb6a1c1b0c4
Author: Marina Glancy <marina@moodle.com>
Date:   Thu Feb 28 12:29:15 2013 +1100

    MDL-38147 Improvements to caching of course categories, coursecat::get_children() improvements

    - Course categories caches are purged by event now
    - session cache has additional 10 minutes ttl to clear itself and accomodate for permission changes that do not trigger event purging
    - additional request-level cache for coursecat::get()
    - We store only children of one category in one cache key
    - Function coursecat::get_children() can return results sorted and/or paginated. Added tests