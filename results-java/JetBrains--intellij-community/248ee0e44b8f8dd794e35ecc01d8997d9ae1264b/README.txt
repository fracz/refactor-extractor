commit 248ee0e44b8f8dd794e35ecc01d8997d9ae1264b
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Oct 19 23:58:46 2016 +0300

    PY-17265 Do not search for a movable function or a reference to it under the caret manually

    It was necessary when "Make  ... Top-Level" was a general refactoring
    action, but now by MoveHandler's machinery takes care of it.