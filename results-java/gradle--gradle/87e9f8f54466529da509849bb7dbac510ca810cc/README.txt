commit 87e9f8f54466529da509849bb7dbac510ca810cc
Author: Lari Hotari <lari.hotari@gradleware.com>
Date:   Tue Apr 28 16:20:17 2015 -0400

    refactor file watcher to improve separation of concerns
    - remove FileWatcherStopper and replace with CountDownLatch in FileWatcherTask
    - rename WatchHandler to WatchListener
    - remove adding of watches in FileWatcherTask
    - move FileWatcherTask to it's own file (top level)