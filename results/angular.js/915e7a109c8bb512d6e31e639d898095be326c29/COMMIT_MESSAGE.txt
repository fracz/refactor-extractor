commit 915e7a109c8bb512d6e31e639d898095be326c29
Author: Martin Staffa <mjstaffa@googlemail.com>
Date:   Wed Jun 15 15:12:30 2016 +0200

    chore(version-info): make online build compatible with Windows

    Since we cannot write to dev/null on Windows, don't write to a file at all, but simply use stdout and extract the output from --write-out from the process response. Credit to @petebacondarwin for the idea.

    The commit also avoids a premature error when no cdnVersion could be found online, and improves the log wrt the origin of the versions.

    PR (#14780)