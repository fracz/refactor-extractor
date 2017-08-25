commit 6f90f13d332cc8205a66ef039db36dc2b9c655ae
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Sat May 30 23:59:45 2015 +0200

    fixed and improved Filesystem

    the tests of the Filesystem had problems on Windows. These have been fixed
    by not relating to /tmp/ and similar folders.

    additionally the tests were also flawed due to the random file-path usage.

    additionally the code has been streamlined between functions and early
    exits have been introduced where possible.