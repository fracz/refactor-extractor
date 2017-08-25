commit d7245e3400206e6bd3ef99c8aa30ad20c1925501
Author: Petr Škoda <commits@skodak.org>
Date:   Thu Jul 4 19:09:21 2013 +0200

    MDL-40475 add alternative component cache location and other improvements

    Improvements include:
    * Alternative location might be useful when server administrator wants to maintain
      a local copy of component cache instead of using shared $CFG->cachedir.
    * Component caching is now enabled in behat tests which should improve performance.
    * Standardised ignoring of component caching.
    * Fixed debug mode in ABORT_AFTER_CONFIG scripts.
    * General documentation improvements.