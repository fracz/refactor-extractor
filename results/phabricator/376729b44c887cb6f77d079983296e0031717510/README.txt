commit 376729b44c887cb6f77d079983296e0031717510
Author: epriestley <git@epriestley.com>
Date:   Wed Dec 31 11:50:20 2014 -0800

    Don't check "repository.default-local-path" for readability in a cluster environment

    Summary:
    Ref T2783. When repository services are defined, skip the check for local repository storage.

    In particular, in the Phacility cluster, this path won't exist on web nodes, but nothing will ever try to access it.

    Test Plan: Verified new branch gets hit with cluster services defined.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T2783

    Differential Revision: https://secure.phabricator.com/D11100