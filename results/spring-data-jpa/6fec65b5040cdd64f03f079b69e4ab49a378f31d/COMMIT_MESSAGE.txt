commit 6fec65b5040cdd64f03f079b69e4ab49a378f31d
Author: Oliver Gierke <info@olivergierke.de>
Date:   Tue Sep 6 13:19:59 2011 +0200

    DATAJPA-97 - Added test to verify hints get applied to all queries correctly.

    The actual fix for this ticket has been introduced during the refactoring for DATAJPA-86 (04349d25dc73) thus it's already fixed in 1.1.0.M1. Added a test case to verify the behavior.