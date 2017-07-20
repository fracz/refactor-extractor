commit 39afb3fedb72312d22d076a589c609dbb93a3b68
Author: Bryan Cuccioli <blc72@cornell.edu>
Date:   Mon Feb 4 09:03:19 2013 -0800

    Suggest STRICT_ALL_TABLES during setup. Improve dev-mode comments.

    Summary: Suggest the MySQL mode STRICT_ALL_TABLES during setup if it is not set. Small improvement to the phabricator.developer-mode comments.

    Test Plan: Set the global sql_mode to include or exclude STRICT_ALL_TABLES and check for desired behavior.

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Korvin

    Differential Revision: https://secure.phabricator.com/D4803