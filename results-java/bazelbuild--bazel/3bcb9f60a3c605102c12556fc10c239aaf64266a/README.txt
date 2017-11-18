commit 3bcb9f60a3c605102c12556fc10c239aaf64266a
Author: philwo <philwo@google.com>
Date:   Wed Sep 6 12:52:21 2017 +0200

    More BUILD file refactorings.

    Split collect, concurrent, vfs, windows into package-level BUILD files.
    Move clock classes out of "util", into their own Java package.
    Move CompactHashSet into its own Java package to break a dependency cycle.
    Give nestedset and inmemoryfs their own package-level BUILD files.

    PiperOrigin-RevId: 167702127