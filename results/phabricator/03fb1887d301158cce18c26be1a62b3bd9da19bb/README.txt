commit 03fb1887d301158cce18c26be1a62b3bd9da19bb
Author: epriestley <git@epriestley.com>
Date:   Tue Sep 13 08:08:31 2011 -0700

    Fix file URI perf regression

    Summary:
    The CSRF changes meant that we can't generate a file URI with just its PHID
    anymore, and converted a mathematical function into a service call.
    Unfortunately, this caused massive perf problems in some parts of the
    application, critically handles, where loading N users became N single gets.
    Derp derp derp. Remedy this by doing a single multiget. This substantially
    improves performance of many interfaces, particularly the Maniphest task list.

    I need to go through the rest of the PhabricatorFileURI callsites and get rid of
    them, but I think this is the most substantive one.

    Test Plan: Profiled Maniphest task list, queries went from >100 to a handful.
    Explosion of multiderp. :/ Looked at some views with profile photos to verify
    they still render accurately.

    Reviewers: jungejason, nh, tuomaspelkonen, aran

    Reviewed By: aran

    CC: aran

    Differential Revision: 921