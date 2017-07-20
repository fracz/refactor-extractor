commit f3a2d2b0204346d8bff11fb1709b9293b28461ef
Author: epriestley <git@epriestley.com>
Date:   Wed Apr 29 13:28:48 2015 -0700

    Restore missing ApplicationSearch join clause for Maniphest

    Summary: See IRC. This got dropped in the order refactoring.

    Test Plan: Ordered Maniphest search results by a custom field.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Differential Revision: https://secure.phabricator.com/D12614