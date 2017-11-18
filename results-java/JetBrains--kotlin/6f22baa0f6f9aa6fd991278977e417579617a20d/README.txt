commit 6f22baa0f6f9aa6fd991278977e417579617a20d
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Tue Jun 6 17:55:53 2017 +0300

    REPL: improve "are all errors at EOF" detection

    Also ignore whitespaces and comments that appear after the last error in
    a REPL line

     #KT-15172 Fixed