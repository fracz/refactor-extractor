commit 8298f5cae542e0c5b77336604b2acc870479f085
Author: Andrey Vlasovskikh <andrey.vlasovskikh@jetbrains.com>
Date:   Thu Sep 4 18:07:43 2014 +0400

    Disable move refactoring for non top-level elements in hierarchy trees (PY-13841)

    Move refactoring from hierarchy trees skips tryToMove() and invokes
    just canMove() and doMove().