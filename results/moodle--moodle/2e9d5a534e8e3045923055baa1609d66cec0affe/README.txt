commit 2e9d5a534e8e3045923055baa1609d66cec0affe
Author: Sam Hemelryk <sam@moodle.com>
Date:   Fri Aug 16 11:22:15 2013 +1200

    MDL-41106 cache: several fixes for the session cache.

    This issue makes several fixes for the session loader and the session store.
    * maxsize argument now works for session caches.
    * fixed performance hole when interation occurs frequently.
    * fixed cache purge bug occuring when multiple caches are defined before being used.
    * improved lastaccess handling.