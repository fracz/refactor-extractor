commit d1c3ec629155737bddb3ea94a3dc4325c199c0df
Author: Robert Muir <rmuir@apache.org>
Date:   Tue Apr 21 09:06:44 2015 -0400

    Upgrade to Lucene 5.2 r1675100

    This upgrade is for https://issues.apache.org/jira/browse/LUCENE-6442

    It should improve test reproducibility, especially if you are on a mac
    and want to reproduce a jenkins failure that happened on linux.