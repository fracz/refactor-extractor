commit 53c1b936e5ef81cf4c4e6f3442e6330118524f9f
Author: Andrew Robert Nicols <andrew.nicols@luns.net.uk>
Date:   Tue Mar 12 11:27:45 2013 +0000

    MDL-38661 Course: Add JS category expander.

    This adds a category expanded which:
    * fetches child content in a category tree if it has not already been loaded;
    * toggles relevant classes on the category node to show and hide child content; and
    * applies appropriate animations to improve user experience.