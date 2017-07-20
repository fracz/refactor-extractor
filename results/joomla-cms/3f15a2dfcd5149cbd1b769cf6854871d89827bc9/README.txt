commit 3f15a2dfcd5149cbd1b769cf6854871d89827bc9
Author: Walt Sorensen <photodude@users.noreply.github.com>
Date:   Fri Jun 2 12:49:47 2017 -0600

    improve mobile checks (#15228)

    * improve mobile checks

    only check for resolution in user agent once to determine mobile

    * [cs] fix tabs vs spaces from copy paste

    * fix the copy-paste peak-a-boo semicolon

    * Mobile is different from browser. Fix conditionals

    * continue moving mobile checks to a separate conditional

    * attempt fix for Unknown modifier on lines 238/280

    * combining mobileexplorer