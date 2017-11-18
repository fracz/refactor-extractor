commit 74f24c1cee9e0522b3a0fdcfe40e47191f3c3973
Author: Kristina Chodorow <kchodorow@google.com>
Date:   Mon May 16 17:13:52 2016 +0000

    Always unzip paths ending in / as directories

    Does file type mean nothing to people?

    Fixes #1263, also made some code hygiene improvements:

    * Added some tests.
    * Broke a java_test out of lib/BUILD.
    * Renamed ZipFunction so it doesn't look like a child of SkyFunction.

    --
    MOS_MIGRATED_REVID=122431322