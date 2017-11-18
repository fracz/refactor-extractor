commit 88e3ca04d967d62e3881aa86585365c4fe405306
Author: Cedric Champeau <cedric@gradle.com>
Date:   Wed May 11 09:43:16 2016 +0200

    Turn stateless transient objects into constants

    Profiling showed that we're creating those objects a lot, without any need for them to be independent instances. Turning them into constants improves memory usage and performance.

    +review REVIEW-5952