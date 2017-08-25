commit 3f47974fc09d079756b6b1a7a6f265eb7661e6cd
Author: Sam Hemelryk <sam@moodle.com>
Date:   Tue Aug 27 10:43:55 2013 +1200

    MDL-41432 output: several action_menu improvements

    The following improvements have been made:
    * Items no longer MUST have an icon, it can now be null instead.
    * You can specify the icon displayed to toggle the dropdown menu.
    * You can add text in front of the aforementioned icon.