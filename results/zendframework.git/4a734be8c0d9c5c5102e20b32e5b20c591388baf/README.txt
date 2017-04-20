commit 4a734be8c0d9c5c5102e20b32e5b20c591388baf
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Wed Sep 22 16:45:30 2010 -0400

    Added ClassMapAutoloader documentation

    - Added documentation for the ClassMapAutoloader
    - Slightly refactored bin/zfal.php and renamed to bin/classmap_generator.php
      - No longer takes -k or -d options; assumes paths will be written using
        __DIR__, and that map is relative to the library directory.
    - Added documentation for bin/classmap_generator.php