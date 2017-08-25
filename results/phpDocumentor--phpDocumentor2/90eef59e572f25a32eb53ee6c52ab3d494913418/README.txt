commit 90eef59e572f25a32eb53ee6c52ab3d494913418
Author: Mike van Riel <me@mikevanriel.com>
Date:   Fri Jun 13 08:55:28 2014 +0200

    Generating XML crashes on Types

    Due to a recent refactoring in the way types are processed will
    phpDocumentor now crash. This commit fixes that by supporting both
    Descriptors and string.