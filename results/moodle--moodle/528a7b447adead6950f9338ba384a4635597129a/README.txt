commit 528a7b447adead6950f9338ba384a4635597129a
Author: Petr Škoda <commits@skodak.org>
Date:   Sat Aug 3 10:48:15 2013 +0200

    MDL-41017 improve purify caching

    The improvements include:

    * HTMLPurifier cache is stored in localcachedir
    * allowobjectembed changes are not ignored any more
    * the cache keys include revision and all options which makes
      this suitable for local caches on cluster nodes
    * unchanged test is replaced by "true" value which should
      significantly improve performance
    * removal of purge_all_caches() hack for directory recreation
    * comments and coding style cleanup