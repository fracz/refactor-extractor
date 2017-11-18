commit 7eaa816646c2faa488b26c709ab173512b91cf37
Author: Googler <noreply@google.com>
Date:   Mon Aug 1 15:48:01 2016 +0000

    Tolerate missing field value in binary's R.txt

    The binary's R.txt might not be superset of
    symbols in the various library R.txt files.

    The R.java writer had tolerated this, as did an
    older version of the RClassGen (lost in refactoring).
    Bring back the null check.

    This can happen if the binary has res overrides that
    *remove* symbols. E.g., if noop/res/layout/stats.xml
    overrides dev/.../stats.xml, and the noop version
    removes elements not needed for production.
    Test mocks can do something similar.

    --
    MOS_MIGRATED_REVID=128989080