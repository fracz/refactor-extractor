commit f37832aed753df3b629eb7d3ee343287c76625d8
Author: epriestley <git@epriestley.com>
Date:   Wed Dec 18 17:48:29 2013 -0800

    Fix loop in svnserve workflow for large binaries

    Summary: If you push a large binary and the data crosses multiple data frames, we can end up in a loop in the parser.

    Test Plan:
    After this change, I was able to push a 95MB binary in 7s, which seems reasonable:

      >>> orbital ~/repos/INIS $ svn st
      A       large2.bin
      >>> orbital ~/repos/INIS $ ls -alh
      total 390648
      drwxr-xr-x   6 epriestley  admin   204B Dec 18 17:14 .
      drwxr-xr-x  98 epriestley  admin   3.3K Dec 16 11:19 ..
      drwxr-xr-x   7 epriestley  admin   238B Dec 18 17:14 .svn
      -rw-r--r--   1 epriestley  admin    80B Dec 18 15:07 README
      -rw-r--r--   1 epriestley  admin    95M Dec 18 16:53 large.bin
      -rw-r--r--   1 epriestley  admin    95M Dec 18 17:14 large2.bin
      >>> orbital ~/repos/INIS $ time svn commit -m 'another large binary'
      Adding  (bin)  large2.bin
      Transmitting file data .
      Committed revision 25.

      real  0m7.215s
      user  0m5.327s
      sys   0m0.407s
      >>> orbital ~/repos/INIS $

    There may be room to improve this by using `PhutilRope`.

    Reviewers: wrotte, btrahan, wotte

    Reviewed By: wotte

    CC: aran

    Differential Revision: https://secure.phabricator.com/D7798