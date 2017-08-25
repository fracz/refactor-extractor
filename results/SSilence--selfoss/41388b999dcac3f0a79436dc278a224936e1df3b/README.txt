commit 41388b999dcac3f0a79436dc278a224936e1df3b
Author: Alexandre Rossi <alexandre.rossi@gmail.com>
Date:   Wed Oct 8 23:57:11 2014 +0200

    fix cannot login when not using HTTPS (fix #581)

    Auth refactoring uncovered a bug in $cookie_secure boolean evaluation.

    Thanks @PhrozenByte for spotting !