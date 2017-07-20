commit 5592e59f928011dbd5c880a0bf3bb755f55d2afd
Author: epriestley <git@epriestley.com>
Date:   Fri Jan 8 06:38:25 2016 -0800

    Improve error message if local Git working copy directory exists but isn't a working copy

    Summary: Fixes T9701. I don't want to try to autofix this because destroying the directory could destroy important files, but we can improve the error message.

    Test Plan: Faked a failure, ran `repository update X`, got a more tailored error message.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9701

    Differential Revision: https://secure.phabricator.com/D14971