commit c2d2fb73095a32394dd95e72421aec49fa2cd6f6
Author: Dries Buytaert <dries@buytaert.net>
Date:   Tue May 13 18:36:38 2003 +0000

    - Fixed a typo in the PostgreSQL database scheme.  Patch by Michael Frankowski.

    - Fixed a typo in the MSSQL database scheme.  Patch by Michael Frankowski.

    - Removed dependency on "register_globals = on"!  Patches by Michael Frankowski.

      Notes:

      + Updated the patches to use $foo["bar"] instead of $foo['bar'].
      + Updated the INSTALL and CHANGELOG files as well.

    - Tiny improvement to the "./scripts/code-clean.sh" script.