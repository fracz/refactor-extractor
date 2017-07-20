commit 5e184ee593b3c0d2ca28ec4c21c70b14442136f7
Author: epriestley <git@epriestley.com>
Date:   Wed Jun 20 13:20:47 2012 -0700

    Improve debug support for notifications

    Summary: Add a `notification.debug` setting that shows debug info in the browser. Also improve some logging/error handling stuff and fix a bug with host names.

    Test Plan: {F13098}

    Reviewers: jungejason, btrahan, vrana

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T944

    Differential Revision: https://secure.phabricator.com/D2810