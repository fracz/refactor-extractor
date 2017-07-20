commit 2adf09fea9f8ba56bab25a5e791fa176fa85538d
Author: Matt Thomas <iammattthomas@gmail.com>
Date:   Wed Nov 13 18:00:10 2013 +0000

    Bring in the responsive component of MP6.  See #25858.

    * Makes the admin fully responsive down to 320px wide.
    * Adds a touch-optimized main menu that can be opened and closed from the toolbar.
    * Size and positioning adjustments to icons, buttons, and text elements for better touch usability.

    A few changes since MP6:

    * Removed jQuery mobile. This script was used to add swipe controls to open/close the sidebar menu. This feature was apparently buggy and due to the pending demise of jQuery mobile, it was removed.
    * Removed use of Backbone.js. Adding Backbone.js to this script would add a dependency of Backbone.js for all of the admin. Additionally, it was used to add a menu item. Instead of doing that, it was added via the admin menu API. This also fixes a bad delay in the item showing in the menu.
    * CSS layout is standardized. Comments have also been cleaned up.
    * Jetpack and Akismet code is removed.
    * RTL CSS is removed.
    * JS passes hinting other than one small issue that will likely be removed when parts of the code are reviewed.

    A number of areas for improvement remain; we're tracking these issues in the comments of #25858.

    Props to tollmanz, tillkruess, helen, dd32, and apeatling.


    Built from https://develop.svn.wordpress.org/trunk@26134


    git-svn-id: http://core.svn.wordpress.org/trunk@26046 1a063a9b-81f0-0310-95a4-ce76da25c4cd