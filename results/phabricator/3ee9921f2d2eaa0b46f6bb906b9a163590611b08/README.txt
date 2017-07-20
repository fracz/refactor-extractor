commit 3ee9921f2d2eaa0b46f6bb906b9a163590611b08
Author: Bob Trahan <bob.trahan@gmail.com>
Date:   Tue Jan 29 16:52:39 2013 -0800

    improve image hinting for 220 preview

    Summary: break out the calculation of dimensions as a static method and use it

    Test Plan: made a conpherence with many images and noted i auto-scrolled to the bottom correctly

    Reviewers: chad, epriestley

    Reviewed By: epriestley

    CC: aran, Korvin

    Maniphest Tasks: T2399

    Differential Revision: https://secure.phabricator.com/D4733