commit a4a1273d3af820bc5f9efcd053b811033b00f282
Author: Aaron Jorbin <aaron@jorb.in>
Date:   Fri Jan 23 18:18:26 2015 +0000

    Improve accessibility of nav menu locations form.

    A couple of small tweaks to the nav menu locations form that make it friendlier to assistive technology.  These include:
     * labels for selects
     * better context for the "Edit" link.  We hide "edit" from screen readers and instead give them a phrase with context since they may not be able to take advantage of the visual context

    Additionally, there are some minor css tweaks to improve the visual alignment of the rows.
    We also remove duplicate IDs and use classes instead.

    Props afercia.
    fixes #31090.


    Built from https://develop.svn.wordpress.org/trunk@31272


    git-svn-id: http://core.svn.wordpress.org/trunk@31253 1a063a9b-81f0-0310-95a4-ce76da25c4cd