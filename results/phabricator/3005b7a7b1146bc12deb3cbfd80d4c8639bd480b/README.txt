commit 3005b7a7b1146bc12deb3cbfd80d4c8639bd480b
Author: Chad Little <chad@chadsdomain.com>
Date:   Wed Apr 2 21:49:28 2014 -0700

    Mobile Differential Diff Review (2-up)

    Summary:
    This does two things

     - Modernizes Table of Contents
     - Makes Differential reasonable on mobile

    I say resonable, as you still have to scroll horizontal to see the entire diff. This is minor as the rest of the page is 100x more useful. A 1-up view would be preferred, but this is still an improvement.

    Test Plan: Used iOS simulator for browsing diffs.

    Reviewers: btrahan, epriestley

    Reviewed By: epriestley

    Subscribers: epriestley, Korvin, chad

    Differential Revision: https://secure.phabricator.com/D8681