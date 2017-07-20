commit 985eb26c7e32cdc19536769def6ab037645100fb
Author: epriestley <git@epriestley.com>
Date:   Wed May 20 14:20:53 2015 -0700

    Increase severity of bin/remove destroy warning

    Summary:
    Make sure we're 100% clear that this is really, truly not recommended.

    Also improve the text itself, and show the objects which are being destroyed more clearly.

    Test Plan: Removed objects with `bin/remove destroy`.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Differential Revision: https://secure.phabricator.com/D12952