commit 7adac642ff5bc5da67e7a549af981ebc1ccd7f91
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Dec 4 19:11:08 2012 +0100

    REVIEW-595 and more. JUnit xml report improvements.

    The report generation is more efficient, it operates on char arrays, directly streaming to the writer. The CDATA processing is efficient, too. The test outputs are maintained in text files.