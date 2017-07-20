commit c970f7e89c428e2359abc9d8d54a95ae32d6fb3f
Author: Bob Trahan <bob.trahan@gmail.com>
Date:   Tue Dec 11 17:16:11 2012 -0800

    refactor pass 2 of N on DifferentialChangesetParser and bonus bug fix

    Summary: pull out some more stuff from the TwoUp renderer that's generically useful. also clean up $xhp variable and add a get method for the $coverage. Finally, fix T2177 while I'm in here.

    Test Plan: played around with Differential. Also opened things up in Firefox to verify T2177.

    Reviewers: epriestley, vrana

    Reviewed By: epriestley

    CC: aran, Korvin

    Maniphest Tasks: T2009, T2177

    Differential Revision: https://secure.phabricator.com/D4161