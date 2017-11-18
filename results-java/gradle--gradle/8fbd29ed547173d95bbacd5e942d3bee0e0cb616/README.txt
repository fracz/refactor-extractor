commit 8fbd29ed547173d95bbacd5e942d3bee0e0cb616
Author: Adam Murdoch <adam@gradle.com>
Date:   Fri Apr 14 07:19:22 2017 +1000

    Changed file snapshotting to assume that files and directories contained in the downloaded artifact and artifact transform output caches do not change during a build, and so to make snapshotting faster.

    File system state cached in-memory during a build for these files and directories is not discarded when a task action runs. This avoids scanning the file system potentially many times during a build over files that have almost certainly not changed. All other file system state is still discarded. This strategy is intentionally very simple and can be improved later.

    This change also means that jars from the artifact transform output cache are not copied into the jar cache when included in a classpath from which Gradle needs to create a `ClassLoader`. This was already the case for jars from the downloaded artifacts cache.