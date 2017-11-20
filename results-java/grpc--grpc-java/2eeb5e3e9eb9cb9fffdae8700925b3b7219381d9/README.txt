commit 2eeb5e3e9eb9cb9fffdae8700925b3b7219381d9
Author: Eric Anderson <ejona@google.com>
Date:   Thu Feb 23 17:02:31 2017 -0800

    all: Downgrade to Guava 19

    Guava 20 introduced some overloading optimizations for Preconditions
    that require using Guava 20+ at runtime. Unfortunately, Guava 20 removes
    some things that is causing incompatibilities with other libraries, like
    Cassandra. While the incompatibility did trigger some of those libraries
    to improve compatibility for newer Guavas, we'd like to give the
    community more time to work through it. See #2688

    At this commit, we appear to be compatible with Guava 18+. It's not
    clear if we want to actually "support" 18, but it did compile. Guava 17
    doesn't have at least MoreObjects, directExecutor, and firstNotNull.
    Guava 21 compiles without warnings, so it should be compatible with
    Guava 22 when it is released.

    One test method will fail with the upcoming Guava 22, but this won't
    impact applications. I made MoreThrowables to avoid using any
    known-deprecated Guava methods in our JARs, to reduce pain for those
    stuck with old versions of gRPC in the future (July 2018).

    In the stand-alone Android apps I removed unnecessary explicit deps
    instead of syncing the version used.