commit 87bd62d22d27390489485105a4f08a9d073380c7
Author: Lisa Luo <luoser@users.noreply.github.com>
Date:   Wed Jan 18 18:00:53 2017 -0500

    Refactor Discovery to properly recover from stopped processes (#48)

    * Modernize DiscoveryVM

    * Modernize DiscoveryFragmentVM

    * Cleanup params logic in DiscoveryVM; refactor output to add Categories

    * Remove redundant Categories api request from fragment; add/update tests

    Pass along the root categories from the activity instead, via the pagerAdapter.

    * Add rootCategories to factory

    * need this.

    * Make outputs BehaviorSubjects rather than PublishSubjects

    * Some Observable refactoring progress

    * Clean up pagerAdapter logic in the DiscoveryVM with delay; rename pageChanged -> pageSelected

    * Update tests with pageSelected vm logic

    * Remove delay; restore to stable behavior

    * Use setPrimaryItem to determine page

    This should fix a bug where restoring the first discovery page after the
    app has been stopped (not killed) results in a blank page.

    The previous approach used `pagerCreatedPage` and `pageSelections`.
    Unfortunately we can't rely entirely on `pageSelections` since it
    will emit page 0 on subscribe, even if a different page is eventually
    restored. `pagerCreatedPage` is also problematic because the fragment
    manager may not *create* the fragment instance again on reload; the
    fragment instance persists and we never get the create event.

    It seems like the best tool available to figure out when a page is ready
    to receive params is to hook into `setPrimaryItem` events. It fires many
    times, so we use `distinctUntilChanged` to de-dupe. The one last wrinkle
    is that it seems like a fragment may not be ready to receive params immediately
    after `setPrimaryItem` is called – I've hooked up a delay so that execution
    here is deferred until the next run loop. It's pretty janky and maybe we
    could spend more time thinking about it since it's not a great pattern.

    * Initial cleanup and test with new inputs

    * Rename page observable, prefer observeOn to delay

    * Observe primaryPage based outputs directly on mainThread to account for Fragment creation delay

    * Make documentation clearer