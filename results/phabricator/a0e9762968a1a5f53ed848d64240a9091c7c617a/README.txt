commit a0e9762968a1a5f53ed848d64240a9091c7c617a
Author: epriestley <git@epriestley.com>
Date:   Mon Mar 2 08:50:36 2015 -0800

    Restore "Query:" to page title in application search

    Summary: Fixes T7055. Omitting this from the crumbs is an improvement, but page titles like "New" seem better with a little more context.

    Test Plan: Saw "Query:" in page titles only.

    Reviewers: chad

    Reviewed By: chad

    Subscribers: epriestley

    Maniphest Tasks: T7055

    Differential Revision: https://secure.phabricator.com/D11931