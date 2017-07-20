commit 97365ea1699e0011380b38ba3ba3e5273ae656f6
Author: Tomasz Narloch <csthomas@users.noreply.github.com>
Date:   Tue Aug 30 07:17:55 2016 +0200

    [Cache] memcache fix for J3.6.2, memcache(-d) performance improvements, less connections, add support for 'platform specific caching' (#11565)

    * Fix memcache storage, refresh code

    * No static var

    * Revert ternary operator

    * Add test whether a $index is an array

    * Merge with #11659

    * Add flush method to storages

    * Replace 'replace' method by 'set' method

    * Sync memcahe changes to memcached

    * Fix issue with clean when getPlatformPrefix is set

    * Fix typo, avoid netsted if

    * Next typo in msg Memcache => Memcached