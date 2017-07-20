commit ee937e99fb9a8310005a4f4e135590400e47df3b
Author: epriestley <git@epriestley.com>
Date:   Mon Oct 5 15:59:16 2015 -0700

    Fix unbounded expansion of allocating resource pool

    Summary:
    Ref T9252. I think there's a more complex version of this problem discussed elsewhere, but here's what we hit today:

      - 5 commits land at the same time and trigger 5 builds.
      - All of them go to acquire a working copy.
      - Working copies have a limit of 1 right now, so 1 of them gets the lease on it.
      - The other 4 all trigger allocation of //new// working copies. So now we have: 1 active, leased working copy and 4 pending, leased working copies.
      - The 4 pending working copies will never activate without manual intervention, so these 4 builds are stuck forever.

    To fix this, prevent WorkingCopies from giving out leases until they activate. So now the leases won't acquire until we know the working copy is good, which solves the first problem.

    However, this creates a secondary problem:

      - As above, all 5 go to acquire a working copy.
      - One gets it.
      - The other 4 trigger allocations, but no longer acquire leases. This is an improvement.
      - Every time the leases update, they trigger another allocation, but never acquire. They trigger, say, a few thousand allocations.
      - Eventually the first build finishes up and the second lease acquires the working copy. After some time, all of the builds finish.
      - However, they generated an unboundedly large number of pending working copy resources during this time.

    This is technically "okay-ish", in that it did work correctly, it just generated a gigantic mess as a side effect.

    To solve this, at least for now, provide a mechanism to impose allocation rate limits and put a cap on the number of allocating resources of a given type. As hard-coded, this the greater of "1" or "25% of the active resources in the pool".

    So if there are 40 working copies active, we'll start allocating up to 10 more and then cut new allocations off until those allocations get sorted out. This prevents us from getting runaway queues of limitless size.

    This also imposes a total active working copy resource limit of 1, which incidentally also fixes the problem, although I expect to raise this soon.

    These mechanisms will need refinement, but the basic idea is:

      - Resources which aren't sure if they can actually activate should wait until they do activate before allowing leases to acquire them. I'm fairly confident this rule is a reasonable one.
      - Then we limit how many bookkeeping side effects Drydock can generate once it starts encountering limits.

    Broadly, some amount of mess is inevitable because Drydock is allowed to try things that might not work. In an extreme case we could prevent this mess by setting all these limits at "1" forever, which would degrade Drydock to effectively be a synchronous, blocking queue.

    The idea here is to put some amount of slack in the system (more than zero, but less than infinity) so we get the performance benefits of having a parallel, asyncronous system without a finite, manageable amount of mess.

    Numbers larger than 0 but less than infinity are pretty tricky, but I think rules like "X% of active resources" seem fairly reasonable, at least for resources like working copies.

    Test Plan:
    Ran something like this:

    ```
    for i in `seq 1 5`; do sh -c '(./bin/harbormaster build --plan 10 rX... &) &'; done;
    ```

    Saw 5 plans launch, acquire leases, proceed in an orderly fashion, and eventually finish successfully.

    Reviewers: hach-que, chad

    Reviewed By: chad

    Maniphest Tasks: T9252

    Differential Revision: https://secure.phabricator.com/D14236