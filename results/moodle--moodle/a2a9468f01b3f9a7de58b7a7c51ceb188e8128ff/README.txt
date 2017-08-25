commit a2a9468f01b3f9a7de58b7a7c51ceb188e8128ff
Author: Sam Hemelryk <sam@moodle.com>
Date:   Mon Nov 3 13:34:00 2014 +1300

    MDL-48026 output: action_menu no wrap improvement

    Action menu output component has a new method set_nowrap_on_items
    that sets a property to toggle nowrap on menu items.
    That can be used to avoid problems when the menu appears within
    an absolutely positioned or floated element.