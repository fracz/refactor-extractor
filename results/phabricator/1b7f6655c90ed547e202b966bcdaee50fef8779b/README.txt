commit 1b7f6655c90ed547e202b966bcdaee50fef8779b
Author: Alan Huang <alanh@fb.com>
Date:   Wed Aug 22 08:19:38 2012 -0700

    Aggregate Differential notifications

    Summary:
    Just a bunch of copy-pasta from D2884. I suppose this calls for
    a refactoring at some point...

    Test Plan:
    Make a bunch of updates, some from different users; check
    notifications dropdown and list.

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Korvin

    Differential Revision: https://secure.phabricator.com/D3361