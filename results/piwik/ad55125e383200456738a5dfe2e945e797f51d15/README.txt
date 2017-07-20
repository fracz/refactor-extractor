commit ad55125e383200456738a5dfe2e945e797f51d15
Author: robocoder <anthon.pang@gmail.com>
Date:   Tue Feb 2 16:10:30 2010 +0000

    fixes #1137 - empty sparkline with floats

     * Visualization/Sparkline.php: back out our SetYMax(+.5) hack and upcase function name.
     * Goals/Goals.php: refactoring
     * libs/sparkline
       * update license files to reflect dual licensing
       * fix clipping issue, ref: https://sourceforge.net/support/tracker.php?aid=2842183
       * fix Y range calculation, ref: https://sourceforge.net/support/tracker.php?aid=2944691


    git-svn-id: http://dev.piwik.org/svn/trunk@1822 59fd770c-687e-43c8-a1e3-f5a4ff64c105