commit 43592c3e49aec3c1c94aa588d5db3b35efc0d960
Author: Skylot <skylot@gmail.com>
Date:   Sun Aug 9 12:29:33 2015 +0300

    gui: improve memory usage (#79)

    - don't use suffix tree in search
    - decrease default working threads count (only 1 for background jobs)
    - use string refs for store only one code string without duplicates
    - use cache for creating UI nodes
    - allow to disable autostart for background jobs (decompilation and index)