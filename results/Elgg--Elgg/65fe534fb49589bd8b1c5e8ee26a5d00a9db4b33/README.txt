commit 65fe534fb49589bd8b1c5e8ee26a5d00a9db4b33
Author: Steve Clay <steve@mrclay.org>
Date:   Fri Dec 12 00:22:55 2014 -0500

    perf(entities): adds preload_containers option to elgg_get_entities

    This allows devs to preload the containing entities of a fetched list. It
    refactors the EntityPreloader a bit to support both use cases, and adds
    container preloading to lists of group discussions.

    Fixes #7663