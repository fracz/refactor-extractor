commit 51157daf4256d60ea8a8a776b6d9c60faef852c7
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Mon Jun 12 13:22:09 2017 +0200

    Refactor lifecycle for lookup adapters and caches (#3873)

    * add initial lifecycle to caches and make them services

    we use auxiliary servicemanagers to start lookup tables and use listeners to maintain the dependent services' state

    * refactoring of lookup table entity lifecycle

    caches and data adapters now run on their own, without being lazily loaded when a lookup table uses them
    this greatly simplfies the update and creation logic and makes it trivial to query the system state

    not all update/delete events are filled in the resource yet

    * add error states to caches (no users yet)

    expose errors in errorstate api endpoint

    * actually start caches/adapters when updating

    avoid race condition by waiting for a running service before re-creating lookup tables

    * Fall back to a DB check in LookupTableService#hasTable()

    The live tables might not be populated yet.

    * send proper cluster events for cache and adapter updates

    * improve service logging by adding the object identity

    fix wrong state in logging service listener

    * make starting adapters/caches resilient against exceptions in doStart()

    improve logging with object ids

    * Use property for throwable-interfaces version

    * convert caches from loading to manual caches

    since we are sharing instances across lookup tables, the caches and adapters no longer have a back reference to the table
    this means the lookup table needs to orchestrate loading and caching

    only loading caches can use refreshAfterWrite, so we had to remove that option from the config

    make sure restarting caches and adapters is properly synchronized by waiting for all instances to be running

    * Remove the now unused throwable-interfaces dependency

    * Create a LookupCacheKey to be used as key for the lookup caches

    The LookupCacheKey consists of the actual key object and a prefix String
    to make it possible to cache the same key value for a different data
    adapter. Since cache instances can be shared between data adapters,
    cache values might be overwritten for different data adapters otherwise.

    * Move data adapter refresh handling into LookupDataAdapterRefreshService

    This decouples the data adapters from the caches and handles the refresh
    logic outside of the data adapter.

    * Remove "<p>" from license comment to make license:check happy

    * Fix eslint errors

    * Add JsonProperty and JsonCreator annotations to LookupCacheKey