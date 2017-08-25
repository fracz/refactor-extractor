commit 7b1f156cd5756883b196767f2abe0f5b53ec8965
Author: David Persson <davidpersson@gmx.de>
Date:   Sun Dec 22 15:16:39 2013 +0100

    Making $data parameter required in Set::matches.

    Refs #993.

    This has been a leftover of a previous refactor (9084d697).
    This patch changes signature as per documentation.

    It can be expected that as the second parameter must be given, the first
    must also always be given, so this doesn't break BC.