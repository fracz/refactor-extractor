commit 993e9d136ea8ada69a59d4e711086c1a35faa8ce
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Sun Jul 17 00:26:14 2016 +0200

    Fix bug in function name comparison.

    AbstractFunctionRestrictionsSniff did not allow for case-insensitive comparison of function names.

    > **Note**: Function names are case-insensitive, though it is usually good form to call functions as they appear in their declaration.

    Ref: http://php.net/manual/en/functions.user-defined.php

    Also:
    * Better be safe than sorry, so `preg_quote()`-ing the function names passed to this class.
    * Minor memory management improvement: no need to remember the matched function name.