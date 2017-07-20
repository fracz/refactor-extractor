commit b483c11d461be1042d2235277d44e29dd506e81e
Author: epriestley <git@epriestley.com>
Date:   Wed Jun 5 11:49:32 2013 -0700

    Fix a bug where PhabricatorPasteQuery incorrectly rekeys results

    Summary: I made changes here recently to improve robustness in the presence of missing files, but accidentally caused the results to re-key. Some callers depend on the mapping, and every other query is consistent about it. Restore the original behavior.

    Test Plan: `Pnnn` works again in remarkup.

    Reviewers: dctrwatson, chad

    Reviewed By: chad

    CC: aran

    Differential Revision: https://secure.phabricator.com/D6134