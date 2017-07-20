commit d2b01aead048f670da04a16f87346321d99d454a
Author: epriestley <git@epriestley.com>
Date:   Tue May 8 12:53:41 2012 -0700

    Use one daemon to discover commits in all repositories, not one per repository

    Summary:
    See D2418. This merges the commit discovery daemon into the same single daemon, and applies all the same rules to it.

    There are relatively few implementation changes, but a few things did change:

      - I simplified/improved Mercurial importing, by finding full branch tip hashes with "--debug branches" and using "parents --template {node}" so we don't need to do separate "--debug id" calls.
      - Added a new "--not" flag to exclude repositories, since I switched to real arg parsing anyway.
      - I removed a web UI notification that you need to restart the daemons, this is no longer true.
      - I added a web UI notification that no pull daemon is running on the machine.

    NOTE: @makinde, this doesn't change anything from your perspective, but it something breaks this is the likely cause.

    This implicitly resolves T792, because discovery no longer runs before pulling.

    Test Plan:

      - Swapped databases to a fresh install.
      - Ran "pulllocal" in debug mode. Verified it correctly does nothing (fixed a minor issue with min() on empty array).
      - Added an SVN repository. Verified it cloned and discovered correctly.
      - Added a Mercurial repository. Verified it cloned and discovered correctly.
      - Added a Git repository. Verified it cloned and discovered correctly.
      - Ran with arguments to verify behaviors: "--not MTEST --not STEST", "P --no-discovery", "P".

    Reviewers: btrahan, csilvers, Makinde

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T792

    Differential Revision: https://secure.phabricator.com/D2430