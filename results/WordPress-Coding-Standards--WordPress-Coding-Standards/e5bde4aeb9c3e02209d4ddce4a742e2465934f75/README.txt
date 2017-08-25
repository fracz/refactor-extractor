commit e5bde4aeb9c3e02209d4ddce4a742e2465934f75
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Tue Jan 24 08:12:33 2017 +0100

    Add new utility function to deal with PHP 5.2 vs T_INLINE_HTML bug.

    This functionality is needed by several of the Theme sniffs, so makes sense to refactor this to a utility function.