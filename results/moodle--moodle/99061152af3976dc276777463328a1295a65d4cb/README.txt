commit 99061152af3976dc276777463328a1295a65d4cb
Author: Damyon Wiese <damyon@moodle.com>
Date:   Thu Oct 6 14:10:14 2016 +0800

    MDL-55074 theme_boost: Navigation and blocks

    This patch includes a big set of changes that are all designed to work together to provide
    a better way to navigate in the new theme, and a different way of working with blocks.

    Blocks have been moved to a "drawer" that can be opened and closed (this is remembered in a user pref).

    A new "flat navigation" element is also available in a drawer - which should let you do 90% of things
    without needing to open the "blocks" drawer.

    The flat navigation is build from specific parts of the nav tree - the top nodes like "calendar, dashboard" are
    hand picked. There is a mycourses node listing your enrolled courses.

    There is a node for the current course, built from the top nodes in the current course node in the nav tree.

    Administrators have a link to the Site admin settings here too.

    These nav elements are used by the templates for the new theme, which also has a resigned layout for login and signup.

    There have also been some additional fixes / improvements to the scss for the new theme which goes along with these
    layout changes.

    This set of changes is a collaboration between Martin, Damyon and Alberto (thanks!).