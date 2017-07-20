commit 84742a94db1301a80534fa0d90abb3464cb57aae
Author: epriestley <git@epriestley.com>
Date:   Thu May 25 13:03:41 2017 -0700

    Restore missing feed rendering for Maniphest points transactions

    Summary: See downstream <https://phabricator.wikimedia.org/T166321>. These got dropped in refactoring, or maybe never existed.

    Test Plan: {F4977212}

    Reviewers: chad

    Reviewed By: chad

    Differential Revision: https://secure.phabricator.com/D18018