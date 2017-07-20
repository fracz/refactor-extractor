commit 1887ec69c9230ff3fbde63f209b50ce69b28fc62
Author: Andrey Andreev <narf@bofh.bg>
Date:   Sat Oct 27 16:22:07 2012 +0300

    Input class improvements

    - Disable register_globals replication on PHP 5.4+ (no longer exists).
    - DocBlock improvements.
    - Add missing changelog entry.
    - Change user_agent() to return NULL when no value is found (for consistency with other fetcher methods).