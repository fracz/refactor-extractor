commit 64cd4f969d2a71b8463ee56659b40257b20c93cc
Author: jungejason <jungejason@fb.com>
Date:   Thu Mar 31 18:28:24 2011 -0700

    Add color to diffusion blame and improve plain view

    Summary:
    query the database to get the epoch info for the commits, then
    calculate the color depending on the epoch (the newer the commit, the
    dark its color). Also improved the plain blame view for git, as the
    git-blame doesn't produce a good display by default. Now we format the
    output it from the data we fetches from the database.

    Test Plan:
    verify both git and svn browsing page work for 'plain',
    'plainblame', 'highlighted' and 'highlightedblame' view.

    Reviewed By: epriestley
    Reviewers: epriestley
    CC: epriestley, jungejason
    Differential Revision: 93