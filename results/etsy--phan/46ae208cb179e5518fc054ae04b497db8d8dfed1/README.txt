commit 46ae208cb179e5518fc054ae04b497db8d8dfed1
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Sat Jul 15 17:34:36 2017 -0700

    Use BlockExitStatusChecker to improve overall analysis of control flow.

    Fixes #308 : Type narrowing bleed.

    Related to #817 : Don't merge Contexts if the corresponding blocks
    always return or throw.

    Fixes #996 : When checking if a variable is defined on all branches,
    ignore branches which unconditionally throw/return

    Fixes #956 : Create an (in development) plugin `AlwaysReturnPlugin`
    to check that a method or function will unconditionally return a value.
    There are still edge cases analyzing try/finally, switch statements, loops, etc.

    Fixes #735 : Implement optimizations for BlockExitStatusChecker.
    Rewrite BlockExitStatusChecker to cache the statement or block's exit
    status in \ast\Node->flags (For node kinds which don't have uses for flags).
    This saves memory (Only stored while node is referenced) and is efficient.
    Stop using SplObjectStorage

    Within phan, replace assert(false) with `throw new \AssertionError`
    so that it unconditionally returns,
    and fix switch statements which weren't guaranteed to return.
    (Currently, phan overrides the settings anyway)