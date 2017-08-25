commit 958dc6efa414f17a97c198bd3aaa923d6c820533
Author: Steve Clay <steve@mrclay.org>
Date:   Sun Oct 25 22:05:17 2015 -0400

    chore(core): allow caching almost all data used in boot

    This adds plugin settings prefetching for active plugins, and reorganizes
    the boot sequence to allow caching numerous settings and entities needed.

     - site entity
     - active plugin entities
     - plugin settings
     - datalist
     - config
     - subtype data

    By default, this data is not cached, but a site owner can modify `settings.php`
    to set a non-zero TTL. The cache is stored on disk in the dataroot, or uses
    memcache if enabled. Besides the TTL, several cache invalidation triggers
    exist to ensure this data isn't used after it's stale.

    Elgg's Pool interface now allows requesting values from the cache without providing
    a strategy for populating them.

    The developer tools screen log includes more stats: elapsed time, whether system
    cache is on, and whether the new boot cache was rebuilt for the current request.

    Fixes #4346
    Fixes #9087