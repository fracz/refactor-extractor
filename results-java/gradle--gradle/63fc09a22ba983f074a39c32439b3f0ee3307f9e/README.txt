commit 63fc09a22ba983f074a39c32439b3f0ee3307f9e
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu May 30 00:22:38 2013 +0200

    File lock performance improvements, step 1.

    The file lock is aware of other Gradle processes and is optimised for single Gradle process. This means that the file lock is much faster if only one Gradle process runs build at a given time. If multiple Gradle processes execute builds at given time, the process-level synchronisation might be a little bit slower.

    This change and the next few may break things. I've been testing this inside out for few days now but some integ tests may become flaky, some coverage builds may fail. I haven't tested this on windows. I'll be monitoring those and fix any problems.

    1. DefaultFileLockManager owns a new thread that listens for other processes cache access requests. Only if such request is received, the file lock is released. Otherwise, the owner process keeps hold of the file lock for pretty much the whole build.
    2. DefaultCacheAccess manages lifecycle of opened locks. Not sure it this is a good idea. I'm looking for feedback. I expect that we'll iterate over the changes between DefaultFileLockManager and DefaultCacheAccess and come up with a nice solution.
    3. Some unit test coverage is intentionally missing as I'd like to review the design and cache <-> lock manager interactions.
    4. This change requires a couple of other changes that come next. Because it's pretty meaty, I thought it makes sense to split them.

    Please review!