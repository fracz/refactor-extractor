commit 21c4aeb032cde81b829161d3bbf9648470e25437
Author: epriestley <git@epriestley.com>
Date:   Mon Apr 27 03:50:20 2015 -0700

    Improve low-level branch resolution in Mercurial

    Summary:
    Ref T7100. Ref T7108. Ref T6160. Several issues:

      - High load for mercurial repositories with huge numbers of branches (T7108).
        - In Mercurial, we resolve refs individually (one `hg` call per ref).
        - Each repository update also updates all refs, which requires resolving all of them.
        - For repositories with a huge number of branches,
      - We don't distinguish between closed branches (a Mercurial-only concept) and open branches (T6160).
        - In Git, when a branch is merged, it ceases to exist.
        - In Mercurial, when a branch is merged, it still exists, it's just "closed". Normally, no one cares about these branches.
        - In the low-level query, correctly identify which refs we resolve as branches.
        - In the low-level query, correctly mark closed branches as closed.
      - This marginally improves ref handling in general (see T7100).

    Test Plan:
    {F384366}

    {F384367}

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T6160, T7108, T7100

    Differential Revision: https://secure.phabricator.com/D12548