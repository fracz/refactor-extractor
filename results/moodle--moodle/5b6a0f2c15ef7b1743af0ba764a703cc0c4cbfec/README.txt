commit 5b6a0f2c15ef7b1743af0ba764a703cc0c4cbfec
Author: Ruslan Kabalin <r.kabalin@lancaster.ac.uk>
Date:   Fri Feb 19 09:35:54 2016 +0000

    MDL-50887 antivirus_clamav: Split scanning logic and results processing.

    This refactoring will make possible to assert scan results processing
    behaviour in unit testing.