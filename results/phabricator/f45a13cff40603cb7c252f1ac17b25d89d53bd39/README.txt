commit f45a13cff40603cb7c252f1ac17b25d89d53bd39
Author: epriestley <git@epriestley.com>
Date:   Tue Dec 6 05:02:12 2016 -0800

    Improve settings caches on fast paths like Conduit

    Summary:
    Ref T11954. This reduces how much work we need to do to load settings, particularly for Conduit (which currently can not benefit directly from the user cache, because it loads the user indirectly via a token).

    Specifically:

      - Cache builtin defaults in the runtime cache. This means Phabricator may need to be restarted if you change a global setting default, but this is exceptionally rare.
      - Cache global defaults in the mutable cache. This means we do less work to load them.
      - Avoid loading settings classes if we don't have to.
      - If we missed the user cache for settings, try to read it from the cache table before we actually go regenerate it (we miss on Conduit pathways).

    Test Plan: Used `ab -n100 ...` to observe a ~6-10ms performance improvement for `user.whoami`.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11954

    Differential Revision: https://secure.phabricator.com/D16998