commit 7986444a11df4c29ae06881530c5132fa25a664f
Author: Daz DeBoer <daz@gradle.com>
Date:   Sun Nov 5 11:11:04 2017 -0700

    Only abort repository lookups on critical resolution failure

    Gradle 4.3 introduced an improvement where an error in resolving a module from
    one repository would prevent Gradle from searching for that same module in
    subsequent repositories (see #2853).

    However, the change to abort searching repositories on _all_ unrecognised errors
    proved to be too aggressive. With this change, only repository timeout errors
    will prevent Gradle from searching for a module in a subsequent repository.
    These timeout errors are considered 'critical' and will blacklist the repository
    and abort resolution for that module.

    In a future release of Gradle, it is likely that we will expand the set of resolution
    failures that we consider 'critical' to include server errors (HTTP 500) and the
    like. This commit keeps the set small to miminize impact on the 4.3.1 release.