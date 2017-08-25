commit 2123166d62d2cc9e85c316d3cd804a79a55758c1
Author: martinlanghoff <martinlanghoff>
Date:   Wed Sep 19 07:11:04 2007 +0000

    accesslib: get_user_courses_bycap() fix bug introduced by refactor

    The refactor that created make_context_subobj() triggered a bug.
    Smack in the hand to the sloppy programmer using variables outside
    of the context they were meant to be used in!