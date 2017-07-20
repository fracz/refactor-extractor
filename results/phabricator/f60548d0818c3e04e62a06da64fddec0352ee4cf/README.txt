commit f60548d0818c3e04e62a06da64fddec0352ee4cf
Author: epriestley <git@epriestley.com>
Date:   Thu Dec 10 14:04:18 2015 -0800

    Default "Land Revision" dialog to land into the default branch for the repository

    Summary:
    Ref T9952. Default the branch target in the dialog to be whatever branch is the default branch for the repository.

    This will be correct for repositories like ours (which land everything into `master`) and correct most of the time for repositories which have some other "primary" branch (maybe `development`).

    It won't be great if there are multiple open lines of development in a repository (for example, some changes go to `newdesignpro` and some changes go to `legacy-1.2`). I'll do work in T3462 next to improve those cases so we can pick a better default.

    Test Plan:
      - Saw dialog default to "master".
      - Changed repo default branch, saw it default to "notmaster" instead.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9952

    Differential Revision: https://secure.phabricator.com/D14734