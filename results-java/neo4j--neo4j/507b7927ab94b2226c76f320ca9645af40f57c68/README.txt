commit 507b7927ab94b2226c76f320ca9645af40f57c68
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Thu Feb 16 23:13:29 2017 +0100

    Better error message when db stopped for store copy

    This commit improves error message when users (bolt or embedded) try to
    start a transaction while cluster member is copying store. It is done using a
    special `AvailabilityRequirement` for the `AvailabilityGuard` when local
    database is stopped to perform a store copy.