commit db898834479061f3de672d89cc8fd2f0c6a366c2
Author: epriestley <git@epriestley.com>
Date:   Mon Dec 30 16:48:36 2013 -0800

    Always include the current user as a selectable policy

    Summary: Ref T4136. After Passphrase, user policies work correctly in this dropdown. Providing this option improves consistency and makes it easier to create, e.g., a private repository (where "no one" does not include the viewer, because they don't own the resulting object).

    Test Plan: Set an object's policy to my user policy.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T4136

    Differential Revision: https://secure.phabricator.com/D7858