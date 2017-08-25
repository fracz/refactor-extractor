commit 32c670c8c245a916ede7a8a485af9e332ffed129
Author: David Mudrák <david@moodle.com>
Date:   Tue Nov 18 23:43:21 2014 +0100

    MDL-48210 updates checker: Improve cron notifications processing

    These are cosmetic improvements spotted while working on the issue.

    1. Do not waste time if there are no changes to notify about.
    2. Fix the legacy plugin manager's get_plugins() call.