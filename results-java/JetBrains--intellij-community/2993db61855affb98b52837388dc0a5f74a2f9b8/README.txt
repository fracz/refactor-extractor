commit 2993db61855affb98b52837388dc0a5f74a2f9b8
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Oct 7 18:36:24 2015 +0300

    PY-6637 Handle recursive functions and methods

    Also refactoring doesn't complain about constructor calls of
    top-level classes inside function body.