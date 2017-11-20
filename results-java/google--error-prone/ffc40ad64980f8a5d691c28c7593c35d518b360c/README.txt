commit ffc40ad64980f8a5d691c28c7593c35d518b360c
Author: cushon <cushon@google.com>
Date:   Tue Mar 7 16:42:02 2017 -0800

    Improve FallThrough diagnostics

    The fix snippet logic handles newlines poorly, so it ends up asking "did
    you mean to delete this line" for the current fixes. Since the suggested
    fix shouldn't be blindly applied anyways, this change removes it and
    improves the message.

    MOE_MIGRATED_REVID=149483749