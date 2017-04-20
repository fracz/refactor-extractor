commit 25ea76ff82e1b433bc941a633b0bada0129187f2
Author: Benoît Durand <intiilapa-zf@ssji.net>
Date:   Sun Aug 26 18:19:14 2012 +0200

    Fix BC for extra in Simple Formatter

    ZF2 key is 'extra', Simple Formatter uses the legacy (ZF1) key (=
    'info'). It is not functional with Logger.

    The refactor introduces a base formatter in order to normalize in string
    value all non-scalar data in extra array.