commit 3eaac9eda9d5e08aaf9266b8079d880a012a563f
Author: James Rhodes <jrhodes@redpointsoftware.com.au>
Date:   Fri Nov 8 19:06:45 2013 -0800

    Added `build.id` variable and other improvements / fixes

    Summary: This adds a `build.id` variable, cleans up the naming convention of other variables and also fixes an issue in the remote command to read the buffers after the command finishes.

    Test Plan: Ran a build with `/bin/echo ${build.id}` and saw the build ID come through.

    Reviewers: epriestley, #blessed_reviewers

    Reviewed By: epriestley

    CC: Korvin, epriestley, aran

    Maniphest Tasks: T1049

    Differential Revision: https://secure.phabricator.com/D7540