commit 3d12bbb17c814a0224eaaa8239a101efd663dba4
Author: epriestley <git@epriestley.com>
Date:   Mon Apr 21 16:28:59 2014 -0700

    Minor, improve notification resiliance

    Summary: The token transactions can publish empty transaction feed stories.
    Stop them from doing that, and make notifications fail more quietly.

    Auditors: btrahan