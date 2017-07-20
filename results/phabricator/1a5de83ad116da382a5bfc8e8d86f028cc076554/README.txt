commit 1a5de83ad116da382a5bfc8e8d86f028cc076554
Author: Neal Poole <neal@fb.com>
Date:   Fri Oct 4 00:28:52 2013 -0700

    Add support for bookmarks in Phabricator emails.

    Summary: Right now emails don't include bookmark info (wasn't added in D2897). Lets include it so it's consistent with the web UI.

    Test Plan: Inspected code, made sure it matched web UI code. Verified that web UI with these changes was consistent with rendering before refactoring.

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: Korvin, aran

    Differential Revision: https://secure.phabricator.com/D7215