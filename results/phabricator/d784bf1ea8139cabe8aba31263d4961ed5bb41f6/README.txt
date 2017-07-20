commit d784bf1ea8139cabe8aba31263d4961ed5bb41f6
Author: epriestley <git@epriestley.com>
Date:   Mon Oct 19 11:14:46 2015 -0700

    Make disk-based setup caches more correct (but slower)

    Summary:
    Fixes T9599. When APC/APCu are not available, we fall back to a disk-based cache.

    We try to share this cache across webserver processes like APC/APCu would be shared in order to improve performance, but are just kind of guessing how to coordinate it. From T9599, it sounds like we don't always get this right in every configuration.

    Since this is complicated and error prone, just stop trying to do this. This cache has bad performance anyway (no production install should be using it), and we have much better APC/APCu setup instructions now than we did when I wrote this. Just using the PID is simpler and more correct.

    Test Plan:
      - Artificially disabled APC.
      - Reloaded the page, saw all the setup stuff run.
      - Reloaded the page, saw no setup stuff run (i.e., cache was hit).
      - Restarted the webserver.
      - Reloaded the page, saw all the setup stuff run.
      - Reloaded again, got a cache hit.

    I don't really know how to reproduce the exact problem with the parent PID not working, but from T9599 it sounds like this fixed the issue and from my test plan we still appear to get correct behavior in the standard/common case.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9599

    Differential Revision: https://secure.phabricator.com/D14302