commit 96c51028e50fb3e6fc5f831d7843ed5a6cf25c95
Author: epriestley <git@epriestley.com>
Date:   Fri Jun 17 13:18:22 2016 -0700

    In Harbormaster, release artifacts as soon as no waiting/running build steps will use them

    Summary:
    Ref T11153. If you have a build plan like this:

      - Lease machine A.
      - Lease machine B.
      - Run client-tests on machine A.
      - Run server-tests on machine B.

    ...and we get machine A quickly, then finish the tests, we currently do not release machine A until the whole plan finishes.

    In the best case, this wastes resources (something else could be using that machine for a while).

    In a worse case, this wastes a lot of resources (if machine B is slow to acquire, or the server tests are much slower than the client tests, machine A will get tied up for a really long time).

    In the absolute worst case, this might deadlock things.

    Instead, release artifacts as soon as no waiting/running steps take them as inputs. In this case, we'd release machine A as soon as we finished running the client tests.

    In the case where machines A and B are resources of the same type, this should prevent deadlocks. In all cases, this should improve build throughput at least somewhat.

    Test Plan:
    I wrote this build plan which runs a "fast" step (10 seconds) and a "slow" step (120 seconds):

    {F1691190}

    Before the patch, running this build plan held the lease on the "fast" machine for the full 120 seconds, then released both leases at the same time at the very end.

    After this patch, I ran this plan and observed the "fast" lease get released after 10 seconds, while the "slow" lease was held for the full 120.

    (Also added some `var_dump()` into things to sanity check the logic; it appeared correct.)

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11153

    Differential Revision: https://secure.phabricator.com/D16145