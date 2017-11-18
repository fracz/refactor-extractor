commit 3fbcc611533ba19f283afa3e01240456de9e788e
Author: Lukacs Berki <lberki@google.com>
Date:   Mon Oct 5 12:30:56 2015 +0000

    Add a Constants.TOOLS_PREFIX constant that will serve to redirect the Bazel tools repository.

    This is a no-op refactoring CL. The actual switch will be made once everything passes with the new setup.

    As a side cleanup, change the awkward realAndroidSdk() / realAndroidCrosstoolTop() mechanism to a converter.

    --
    MOS_MIGRATED_REVID=104649067