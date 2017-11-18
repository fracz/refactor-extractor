commit 5c126efc61af36e11e6e240eabde598636c87406
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Mon Nov 4 15:57:11 2013 +0100

    Improves diagnostic output.

    * toString() of Schema Rule Commands return the actual Schema Rule,
      rather than the DynamicRecord that is used to represent it. This
      improves the output of DumpLogicalLog.
    * messages.log now always contain the revision (git hash) of the Kernel.
    * Exceptions from RecoveringIndexProxy includes information about what
      the index is that is recovering (using the index descriptor).

    + some minor formatting fixes in IdGeneratorImpl.