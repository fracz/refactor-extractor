commit d031d3ae329b7f152ed014470285176c34a384e7
Author: epriestley <git@epriestley.com>
Date:   Thu Jun 23 12:01:00 2011 -0700

    Slightly improve UTF-8 handling in Differential

    Summary:
    See comments. I think this will fix the issue, where we end up handling off
    garbage to htmlspecialchars() after highlighting a file we've stuck full of \0
    bytes.

    The right fix for this is to make wordwrap and intraline-diff utf8 aware and
    throw this whole thing away. I'll work on that but I think this fixes the
    immediate issue.

    Test Plan:
    diffed the file with a UTF-8 quote in it and got a reasonable render in
    Differential

    Reviewed By: jungejason
    Reviewers: jungejason, aran, tuomaspelkonen
    CC: aran, jungejason
    Differential Revision: 504