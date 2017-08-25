commit 2640adfa7d0dbadabf9e0037d796bb0dd82784ff
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Sat Jan 14 23:10:27 2017 +0100

    Simplify the WordPress_AbstractClassRestrictionsSniff

    The refactor of the `WordPress_AbstractFunctionRestrictionsSniff` allows for some code simplification in the `WordPress_AbstractClassRestrictionsSniff`.

    The simplification does not contain any functional changes.

    * Defer to parent `process()` method for initial verification & setup.
    * Defer to parent `process_matched_token()` method for the throwing of the error.
    * Use the `$this->tokens` and `$this->phpcsFile` properties now available instead of passing the variables between functions.

    Also:
    * Removed a property which couldn't be used as is anyway (as the parent uses `self` and PHCPS still has to be compatible with PHP 5.2, so we can't use `static`).