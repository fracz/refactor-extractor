commit 5c50e8630164d7d9a1a097f70d2f8bcbf1bd854f
Author: Narayan Kamath <narayan@google.com>
Date:   Thu Nov 24 13:22:40 2016 +0000

    PackageManagerService: Implement packageParser cache in ParallelPackageParser.

    We save about 2800ms of cold startup time over baseline on a marlin,
    and ~1200 ms over the parallel parsing case.

                       warm     cold
                       ---------------
    Baseline         : 1700ms   4300ms
    Parallel         : 1400ms   2700ms
    Cache            : 1000ms   1600ms
    Cache & parallel : 900ms    1500ms

    Note that further changes will improve the speed of cache processing.

    This change also includes support for :
    - a flag that been flipped in code (currently set to false).
    - disabling the cache via a system property.
    - wiping the cache on system upgrades.
    - cache versioning.

    Bug: 30792387

    Test: FrameworksServicesTests
    Test: manual timing

    Change-Id: I281710c110af5307901dd62ce93b515287c91918