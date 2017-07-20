commit bfaa93aa9b2ccea8053a45e8ba9eb098a1986312
Author: epriestley <git@epriestley.com>
Date:   Mon Sep 28 17:57:41 2015 -0700

    Allow Harbormaster build plans to request additional working copies

    Summary:
    Ref T9123. To run upstream builds in Harbormaster/Drydock, we need to be able to check out `libphutil`, `arcanist` and `phabricator` next to one another.

    This adds an "Also Clone: ..." field to Harbormaster working copy build steps so I can type all three repos into it and get a proper clone with everything we need.

    This is somewhat upstream-centric and a bit narrow, but I don't think it's totally unreasonable, and most of the underlying stuff is relatively general.

    This adds some more typechecking and improves data/type handling for custom fields, too. In particular, it prevents users from entering an invalid/restricted value in a field (for example, you can't "Also Clone" a repository you don't have permission to see).

    Test Plan: Restarted build, got a Drydock resource with multiple repositories in it.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9123

    Differential Revision: https://secure.phabricator.com/D14183