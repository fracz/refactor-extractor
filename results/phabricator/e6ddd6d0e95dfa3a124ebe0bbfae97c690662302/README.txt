commit e6ddd6d0e95dfa3a124ebe0bbfae97c690662302
Author: epriestley <git@epriestley.com>
Date:   Mon Dec 5 20:22:46 2016 -0800

    Cache Almanac URIs for repositories

    Summary:
    Ref T11954. This is kind of complex and I'm not sure I want to actually land it, but it gives us a fairly good improvement for clustered repositories so I'm leaning toward moving forward.

    When we make (or receive) clustered repository requests, we must first load a bunch of stuff out of Almanac to figure out where to send the request (or if we can handle the request ourselves).

    This involves several round trip queries into Almanac (service, device, interfaces, bindings, properties) and generally is fairly slow/expensive. The actual data we get out of it is just a list of URIs.

    Caching this would be very easy, except that invalidating the cache is difficult, since editing any binding, property, interface, or device may invalidate the cache for indirectly connected services and repositories.

    To address this, introduce `PhabricatorCacheEngine`, which is an extensible engine like `PhabricatorDestructionEngine` for propagating cache updates. It has two modes:

      - Discover linked objects (that is: find related objects which may need to have caches invalidated).
      - Invalidate caches (that is: nuke any caches which need to be nuked).

    Both modes are extensible, so third-party code can build repository-dependent caches or whatever. This may be overkill but even if Almanac is the only thing we use it for it feels like a fairly clean solution to the problem.

    With `CacheEngine`, make any edit to Almanac stuff propagate up to the Service, and then from the Service to any linked Repositories.

    Once we hit repositories, invalidate their caches when Almanac changes.

    Test Plan:
      - Observed a 20-30ms performance improvement with `ab -n 100`.
      - (The main page making Conduit calls also gets a performance improvement, although that's a little trickier to measure directly.)
      - Added debugging code to the cache engine stuff to observe the linking and invalidation phases.
      - Made invalidation throw; verified that editing properties, bindings, etc, properly invalidates the cache of any indirectly linked repositories.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11954

    Differential Revision: https://secure.phabricator.com/D17000