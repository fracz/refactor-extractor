commit 3a99f97894ef7f7c3172c4b65ad04327dbc11212
Author: Liam Miller-Cushon <cushon@google.com>
Date:   Sat Mar 4 02:27:02 2017 +0000

    Disallow duplicate srcjars in javac-turbine

    Also improve srcjar extraction to use zipfs instead of a temp dir,
    remove the now unused temp dir handling, and use JSR 199 in a slightly
    more idiomatic way.

    This fixes a bug affecting srcjar entries that differ only in case
    when compiling on platforms with case-insensitive filesystems.

    --
    PiperOrigin-RevId: 149175693
    MOS_MIGRATED_REVID=149175693