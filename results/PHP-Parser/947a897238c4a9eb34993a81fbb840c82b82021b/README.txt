commit 947a897238c4a9eb34993a81fbb840c82b82021b
Author: nikic <nikita.ppv@googlemail.com>
Date:   Sun Apr 20 13:16:54 2014 +0200

    Make names in the parser more descriptive

    And improve the code a tad bit in general.

    I left YY2TBLSTATES and YYNLSTATES around, because I don't fully
    understand their role in the action double indexing.