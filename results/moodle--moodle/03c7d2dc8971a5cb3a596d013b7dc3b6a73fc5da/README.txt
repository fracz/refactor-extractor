commit 03c7d2dc8971a5cb3a596d013b7dc3b6a73fc5da
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Mon May 24 14:53:34 2010 +0000

    MDL-22015 Committing Petr's improvement of core_string_manager disk cache

    Version bump needed so that the cache is cleared on upgrade. Cached
    files are now valid PHP files instead of fetching them into string and
    running eval(). Credit goes to skodak.