commit dc71c0629e60acccd39b59538f2e7f5b09b32509
Author: Drae <paul@starstreak.net>
Date:   Thu Jul 5 18:56:14 2012 +0100

    [feature/pagination-as-list] Various fixes and improvements

    Extracted common template code for prosilver as per subsilver2.
    Various other fixups and oversight corrections, changed name
    of the "new" template function and re-introduced existing
    version. Altered on_page to compensate for removal of some
    templating vars from pagination routine.

    PHPBB3-10968