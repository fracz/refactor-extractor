commit 9243ac39c1eceae0a616e0f5d98bde82baf2b29d
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Mon Oct 29 21:04:34 2012 +0400

    IDEA-93848 Fix & refactor Github Open in Browser for multi-root projects.

    * Don't do "project.getBaseDir();", use the root of currently selected file.
    * Find the tracked branch using GitRepository API.
    * Notify more gracefully.