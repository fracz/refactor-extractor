commit fdca684814dacf6b9386b4f0750905978540526a
Author: epriestley <git@epriestley.com>
Date:   Fri Feb 26 12:20:47 2016 -0800

    Slightly improve Buildable list in Harbormaster

    Summary:
    Ref T10457. This makes diffs/revisions show the revision as the buildable title, and commits show the commit as the title.

    Previously, the title was "Buildable X".

    Also makes icons/colors/labels more consitent.

    Test Plan: {F1131885}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10457

    Differential Revision: https://secure.phabricator.com/D15355