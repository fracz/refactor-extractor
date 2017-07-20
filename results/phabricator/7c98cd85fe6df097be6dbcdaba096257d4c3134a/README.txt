commit 7c98cd85fe6df097be6dbcdaba096257d4c3134a
Author: epriestley <git@epriestley.com>
Date:   Thu Dec 10 05:14:54 2015 -0800

    Implement DestructibleInterface for Owners Packages

    Summary:
    Fixes T9945. This is straightforward.

    The two sub-object types are very lightweight so I just deleted them directly instead of loading + delete()'ing (or implementing DestructibleInterface on them, which would require they have PHIDs).

    Also improve a US English localization.

    Test Plan:
      - Used `bin/remove destroy PHID-... --trace` to destroy a package.
      - Verified it was gone.
      - Inspected the SQL in the log for general reasonableness.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9945

    Differential Revision: https://secure.phabricator.com/D14729