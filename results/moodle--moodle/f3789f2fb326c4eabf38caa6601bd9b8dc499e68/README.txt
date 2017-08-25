commit f3789f2fb326c4eabf38caa6601bd9b8dc499e68
Author: Russell Smith <mr-russ@smith2001.net>
Date:   Tue Jun 7 15:15:46 2016 +1000

    MDL-45584 cache: Make identifiers part of the cache creation.

    It is now safe to cache a reference to a cache and expect consistent results.

    Changing identifiers altered cache results where a reference was
    held to the cache. Identifiers have been set to be cached with
    identifiers included so the caches are separate.

    As a consequence of this it was identified that invalidation events
    and identifiers don't easily work together as an event can't determine
    which identifiers should be used for cache invalidation.  So invalidation
    events have been made incompatible with identifiers being set.  No core
    code used this combination as it's not possible to understand any expected
    behaviour.

    Event invalidation for application and session caches was centralised to the same
    location.  The only difference was the name of the lastinvalidation variable. This
    improves support and consistency of invalidation code.