commit d700e7f22d6db173c37aaa4c87bcd692644a870b
Author: Asher Baker <asherkin@gmail.com>
Date:   Fri Nov 8 11:37:57 2013 -0800

    Add support for landing to hosted Mercurial repos.

    Summary: I've kept this as close as possible to the Git version for ease of review and later refactoring of them both together. At minimum, the functions to get the working dir should probably be cleaned up one day.

    Test Plan: Landed a revision.

    Reviewers: epriestley, #blessed_reviewers

    Reviewed By: epriestley

    CC: Korvin, epriestley, aran

    Differential Revision: https://secure.phabricator.com/D7534