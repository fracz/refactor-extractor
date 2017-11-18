commit c6c0c48a0e7e863162da31bddb9f773921216cde
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Tue Jul 15 17:24:30 2014 +0200

    Fix a memory leak in RerferenceCache

    The leak was caused by the fact that when inserting new data in the cache
    noone is cleaning up the old GCed values still present in the cache.
    Indeed the cleanup was triggered only by updating existing node which have
    been GCed or at some extend by getting GCed values.

    This PR also tries to improve the polling when during get/remove by looking
    to the whole queue instead of just removing the single element.