commit 751bd6b8dc12a5a6fdb6b3108f5e3ea86ec741fe
Author: Hussein Jafferjee <hussein@jafferjee.ca>
Date:   Tue Aug 13 22:35:31 2013 -0700

    Replace undefined function canCache with canCacheRequest

    Probably a bug from previous refactoring, just caught it as I tried to override only the response can cache mechanism.