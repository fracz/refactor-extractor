commit 8731408b11a24e3a92188653548f2c90bf866a32
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Fri Mar 11 17:25:11 2016 -0700

    Offer to cache ContentResolver-related Bundles.

    There are a handful of core system services that collect data from
    third-party ContentProviders by spinning them up and then caching the
    results locally in memory.  However, if those apps are killed due to
    low-memory pressure, they lose that cached data and have to collect
    it again from scratch.  It's impossible for those apps to maintain a
    correct cache when not running, since they'll miss out on Uri change
    notifications.

    To work around this, this change introducing a narrowly-scoped
    caching mechanism that maps from Uris to Bundles.  The cache is
    isolated per-user and per-calling-package, and internally it's
    optimized to keep the Uri notification flow as fast as possible.
    Each Bundle is invalidated whenever a notification event for a Uri
    key is sent, or when the package hosting the provider is changed.

    This change also wires up DocumentsUI to use this new mechanism,
    which improves cold-start performance from 3300ms to 1800ms.  The
    more DocumentsProviders a system has, the more pronounced this
    benefit is.  Use BOOT_COMPLETED to build the cache at boot.

    Add more permission docs, send a missing extra in DATA_CLEARED
    broadcast.

    Bug: 18406595
    Change-Id: If3eae14bb3c69a8b83a65f530e081efc3b34d4bc