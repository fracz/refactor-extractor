commit 85008bedd51e7fe0ba74dbe42a6fe2b3a41144d0
Author: Sam Hemelryk <sam@moodle.com>
Date:   Mon Aug 26 09:15:57 2013 +1200

    MDL-41106 cache: several fixes for the session cache.

    This issue makes several fixes for the session loader and the session store.
     * maxsize argument now works for session caches.
     * fixed performance hole when interation occurs frequently.
     * fixed cache purge bug occuring when multiple caches are defined before being used.
     * improved lastaccess handling.

    Big thanks to Marina who contributed the following commits:
     * Always make sure the elements in cache are sorted so we need to remove only elements in the beginning of array
     * Remove expired elements from session store to free memory
     * Minor bug fixes