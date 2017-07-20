commit 191f6d904f21db8dcf844f0e6d60c3202b60cab8
Author: epriestley <git@epriestley.com>
Date:   Mon May 7 13:37:41 2012 -0700

    Fix a bug with "Resign" in Diffusion

    Summary: The PHID list is a list, not a map -- I must have broken this in refactoring or something, since everything else works fine. See D2013.

    Test Plan: Viewed a resignable revision, saw "Resign" (new after this commit), resignd.

    Reviewers: 20after4, btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T904

    Differential Revision: https://secure.phabricator.com/D2417