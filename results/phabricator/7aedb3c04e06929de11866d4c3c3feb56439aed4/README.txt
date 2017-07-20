commit 7aedb3c04e06929de11866d4c3c3feb56439aed4
Author: Chad Little <chad@chadsdomain.com>
Date:   Tue Dec 2 10:24:59 2014 -0800

    Update PHUIObjectItemListView structure for more flexibility

    Summary: Converts PHUIObjectItemView to use display: table rows and columns for more flexible layouts. Slightly increases spacing, improves mobile layouts. Fixes T5502

    Test Plan: Tested in multiple applications and UIExamples. Ran through mobile, tablet, and desktop break points. Used IE8-IE10, Firefox, Chrome, Safari on both Mac and Windows.

    Reviewers: btrahan, epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T5502

    Differential Revision: https://secure.phabricator.com/D10917