commit 9c4091679510256931cfe8305708978ac98a7b7b
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Sun Dec 6 22:20:04 2015 +0300

    Simplify auth filters

    The current implementation of filters is rather cumbersome and
    doesn't really reuses the code.

    This change:

    * separates parsing credentials and authentication of a request
    * reuses the code of setting the security context
    * improves readability of the credentials parsing methods
    * adds additional documentation about the credentials format
     and authentication process