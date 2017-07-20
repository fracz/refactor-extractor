commit 58152c45d119160f4523e4de408a9a9166011cae
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sat Jan 5 16:28:34 2002 +0000

    Integrated Marco's generic/improved cache into Drupal.  Requires an
    SQL update.  See below for more details.

    - Merged the file "cache.inc" into "common.inc".

    - In addition, I renamed the field 'url' in the cache table to
      a more generic 'cid' (cache identifier).  It's no longer for
      URLs only.

    - Made the "cache_set()" function ASNI compliant such that it
      will play nice with other databases such as Postgres.

    - Added some extra input checking.

    - Updated the old caching code in the functions "page_header()"
      and "page_footer()" to use the new, generic cache API.

    - Updated "update.php" to make the required SQL changes.