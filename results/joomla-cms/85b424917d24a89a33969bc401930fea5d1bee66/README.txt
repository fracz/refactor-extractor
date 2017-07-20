commit 85b424917d24a89a33969bc401930fea5d1bee66
Author: Tomasz Narloch <csthomas@users.noreply.github.com>
Date:   Sat Sep 3 12:51:09 2016 +0200

    Reduce waiting time (sleep) in the cache tests (#11875)

    * Reduce tests sleep(5) to sleep(3) for cache

    * cachelittle improvement ... this way run in my server in 0.2 sec

    * reorder

    * Update JCacheStorageCacheliteTest.php

    * Update JCacheStorageCacheliteTest.php

    * file cache test in 0.3 seconds

    * Update JCacheStorageCacheliteTest.php

    * Update JCacheStorageFileTest.php

    * Update JCacheStorageCacheliteTest.php

    * Update JCacheStorageFileTest.php

    * extend the timeout to 5 seconds

    * same thing

    * 0.05 is enough

    * Update JCacheStorageFileTest.php

    * do the same for case cache

    * Standardized optimizations