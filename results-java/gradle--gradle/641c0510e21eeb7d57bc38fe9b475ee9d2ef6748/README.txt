commit 641c0510e21eeb7d57bc38fe9b475ee9d2ef6748
Author: Lari Hotari <lari.hotari@gradleware.com>
Date:   Fri Apr 17 14:32:41 2015 -0400

    refactor FileWatcherExecutor by separating some concerns
    - rename FileWatcherExecutor -> FileWatcherTask to better describe it's role
    - move watch item (directory tree / file) handling to WatchRegistry implementations
    - move file watching details to WatchStrategy implemention
      - makes it possible to adapt other file watching implementations later on
      - java.nio.file.Path is used in internal API so it isn't suitable for pre-JDK7
      - preparing for getting around https://bugs.openjdk.java.net/browse/JDK-7133447 on MacOSX
    - move Windows FILE_TREE WatchService handling to ExtendedWatchServiceWatchStrategy
      and ExtendedDirTreeWatchRegistry
    - change FileWatcherService API: throw IOException from watching
    - registering watches to WatchService is done in calling thread, no need for CountDownLatch

    +review REVIEW-5466