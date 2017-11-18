commit 3ec968e251e049cdc7c7fc2bf913fd0789dea773
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Thu Oct 20 00:25:41 2016 +0300

    PY-21147 Don't use targetContainer param of doMove for destination path

    It seems to be specified only when Move refactoring is launched outside
    of the editor (according to MoveHandler's implementation) and this
    refactoring doesn't support such way to run it anyway