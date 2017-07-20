commit 1e3c10379a4b2190aacbc5e7054333b0fe081c93
Author: Jason Ge <jungejason@fb.com>
Date:   Fri Oct 7 18:25:54 2011 -0700

    Enable typeahead's ondemand on details view page

    Summary:
    the details pages are using preload instead of ondemand for
    typeahead, but the most common actions on the pages are commenting which
    would not need the preloaded info. To improve the performance of the
    pages, turn on ondemand according to the setting in the config file.

    Test Plan: verify it is working with both modes, for both pages.

    Reviewers: epriestley, nh

    Reviewed By: epriestley

    CC: aran, epriestley

    Differential Revision: 995