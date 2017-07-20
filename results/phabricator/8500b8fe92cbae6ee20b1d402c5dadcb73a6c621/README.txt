commit 8500b8fe92cbae6ee20b1d402c5dadcb73a6c621
Author: epriestley <git@epriestley.com>
Date:   Sat Jan 19 19:47:50 2013 -0800

    Fix Ponder exception for adding comments

    Summary: Fixes T2323. Prior to search refactoring (I think), this stuff was loaded implicitly. Now load it explicitly.

    Test Plan: Added a comment to a Ponder answer without an exception.

    Reviewers: btrahan, vrana

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T2323

    Differential Revision: https://secure.phabricator.com/D4536