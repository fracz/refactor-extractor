commit e6973481e2305cc77677bbcfb8931ad48b11cf63
Author: Julia Beliaeva <Julia.Beliaeva@jetbrains.com>
Date:   Fri Feb 17 02:42:38 2017 +0300

    [file-history] introduce base action for file history actions on single commit

    This still does not solve async data loading problem. But this makes later refactoring easier, since most of actions now have a base class. Also it introduces some order into a grim vcs integration developer life.

    Another thing to be noted is that in the old file history this actions worked on a single commit, but when more than one was selected, they just took the first one. This behaviour is really unclear for a user. So this commit changes it in new history (needs to be fixed in the old one as well, later).

    And the last: show diff with local does not work for directories now (that is a regression) and annotate fails on commits that delete the file (and now we have these commits because we are awesome).