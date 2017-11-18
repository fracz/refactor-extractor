commit 6eb906ecc346f52bfbff8a500673ec66612bd385
Author: Jason Monk <jmonk@google.com>
Date:   Wed Mar 29 15:04:25 2017 -0400

    Fix losing status bar icons on config changes

    The DarkIconManager was not correctly calling set when icons
    were added, leaving them blank after a config change until a new
    set came in.

    Do some refactoring to fix this and make it more testable.

    Test: runtest systemui
    Change-Id: I0b231021f2ce7d82a3f84ebb281b4e4fc902a0aa
    Fixes: 35367550