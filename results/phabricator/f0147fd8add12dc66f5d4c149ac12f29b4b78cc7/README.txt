commit f0147fd8add12dc66f5d4c149ac12f29b4b78cc7
Author: epriestley <git@epriestley.com>
Date:   Tue May 20 11:42:05 2014 -0700

    Allow workboards to be filtered with ApplicationSearch

    Summary:
    Ref T4673.

    IMPORTANT: I had to break one thing (see TODO) to get this working. Not sure how you want to deal with that. I might be able to put the element //inside// the workboard, or I could write some JS. But I figured I'd get feedback first.

    General areas for improvement:

      - It would be nice to give you some feedback that you have a filter applied.
      - It would be nice to let you save and quickly select common filters.
      - These would probably both be covered by a dropdown menu instead of a button, but that's more JS than I want to sign up for right now.
      - Managing custom filters is also a significant amount of extra UI to build.
      - Also, maybe these filters should be sticky per-board? Or across all boards? Or have a "make this my default view"? I tend to dislike implicit stickiness.

    Test Plan:
    Before:

    {F157543}

    Apply Filter:

    {F157544}

    Filtered:

    {F157545}

    Reviewers: chad, btrahan

    Reviewed By: btrahan

    Subscribers: qgil, swisspol, epriestley

    Maniphest Tasks: T4673

    Differential Revision: https://secure.phabricator.com/D9211