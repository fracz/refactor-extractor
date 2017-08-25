commit 484c4c6cf36f7bf06035dfadcda21953f7d3c331
Author: Sam Hemelryk <sam@moodle.com>
Date:   Fri Oct 4 12:59:32 2013 +1300

    MDL-31830 course: several management interface improvements

    * Tidied up course detail permissions so that user is not shown information they couldn't access elsewhere.
    * category link dimming now accounts for course creation as an action as well.
    * category single select when in courses view mode is now limited to courses user can action in.
    * There is now a check at the start of the management page to redirect to course/index.php if the user isn't able to manage in any category.
    * Tweaked navigation again, to give the limited users a navbar structure similar to the system cap'd user.
    * Cancelling a category delete now takes you back to the category you were viewing.
    * Fixed undefined notice
    * Improved placement of course request and approval links.
    * Several styling tweaks/improvements to the base theme.
    * Several styling tweaks/improvements to the bootstrapbase theme.