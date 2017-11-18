commit 561e13d687bf698b759cd0dbb653f3250a5b6917
Author: Narayan Kamath <narayan@google.com>
Date:   Thu May 21 10:50:35 2015 +0100

    Fix application moves.

    We don't dex2oat during application moves, so we must scan
    the package again scanPackageDirtyLI to deduce its ABI.

    This is unnecessary (since a move cannot change ABIs), but we
    need some additional refactoring to avoid a second scan.

    bug: 21337469

    (cherry picked from commit cd251fa382e887f59c278d4d7cd0a858812c6114)

    Change-Id: Id1ed3bdfabb41e05e6a2f7efbd05d2103a67c663