commit 89ee928a51c72d9fda17a59f73b060a92cc0c665
Author: epriestley <git@epriestley.com>
Date:   Mon Jul 15 13:18:50 2013 -0700

    Share rendering code for embedded votes and vote detail

    Summary:
    We have two separate pieces of rendering code and both are pretty ugly. Move them toward being more reasonable.

    This could no doubt be improved:

      - Getting a text style which was readable on both the dark and light bars was hard, maybe we should change the colors or maybe I am just bad.
      - Could probably benefit from actual competent design in general.
      - JS magic is temporarily ineffective, I'll restore that in the future.
      - Embed style is a little funky (margin/centering).
      - Could use a little cleanup.

    Test Plan:
    {F50226}
    {F50227}
    {F50228}

    Reviewers: chad, btrahan

    Reviewed By: btrahan

    CC: aran

    Differential Revision: https://secure.phabricator.com/D6465