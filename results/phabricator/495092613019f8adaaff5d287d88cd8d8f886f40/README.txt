commit 495092613019f8adaaff5d287d88cd8d8f886f40
Author: epriestley <git@epriestley.com>
Date:   Wed Dec 7 12:48:04 2016 -0800

    Validate settings before writing them to the user cache

    Summary:
    Fixes T11960. In D16998 I removed some code which validated settings on read to improve performance, but lost this replacement validation in shuffling the patch stack.

    This restores similar validation before we write the cache. This has the same effect, it's just faster.

    Also, bump the cache key to wipe out anything that got bitten (like my account on `secure` rendering dates wrong).

    Test Plan:
      - Edited settings, verified the edits held.
      - Faked invalid settings, saw the check throw exceptions.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11960

    Differential Revision: https://secure.phabricator.com/D17008