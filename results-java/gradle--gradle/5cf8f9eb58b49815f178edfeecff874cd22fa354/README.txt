commit 5cf8f9eb58b49815f178edfeecff874cd22fa354
Author: Adam Murdoch <adam@gradle.com>
Date:   Fri Feb 3 12:59:30 2017 +1100

    Use `stat()` instead of `File` operations `CachingFileHasher.hash(File)`, which is almost always faster and often has a finer-grained resolution for last modified timestamp. Also changed `JarCache` to reuse `FileHasher` instead of reimplementing the same logic that determines whether a file has changed. This logic has moved on quite a bit since `JarCache` was implemented. Now, further improvements to accuracy or performance will be reused by `JarCache`.

    Removed `sleep()` from functional tests as these should no longer be required thanks to the reuse.