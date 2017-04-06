commit b51dd3010dd35330a64dc96bdc1ea8b6f831e0d3
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Wed Oct 7 12:10:07 2015 +0100

    refactor($compile): initialize removeWatchCollection at the start

    This check means that we don't have to keep checking whether the collection
    has been created when adding a new watcher

    Closes #12528