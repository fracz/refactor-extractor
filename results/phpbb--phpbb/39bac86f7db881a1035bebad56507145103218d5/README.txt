commit 39bac86f7db881a1035bebad56507145103218d5
Author: Dhruv <dhruv.goel92@gmail.com>
Date:   Sun Jul 22 03:43:50 2012 +0530

    [feature/sphinx-fulltext-search] improve port option

    Use listen instead of deprecated port value in sphinx config file.
    sqlhost uses default $dbhost.

    PHPBB3-10946