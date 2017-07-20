commit f26fea70c5c6d97fd4f15550ae592d9181641e28
Author: Matt Thomas <iammattthomas@gmail.com>
Date:   Wed Nov 20 22:48:09 2013 +0000

    Making improvements to new styles added in the MP6 merge, and addressing visual feedback from core team.

    Improved and more consistent styling on the widgets & menus pages:

    * Use the same visual treatment for both widgets and menus.
    * Re-use existing styles from input:focus for draggable elements.
    * Use the standard H3 size for widget area titles.
    * Re-style the Widgets page drop zone to match drop zone from dashboard.
    * Replace the gear icon on Widgets with the standard open/close arrows.
    * Remove "Choose a sidebar" header from widget area chooser.

    Make open/close arrows more consistent:

    * Use the same arrow direction everywhere: pointing down when its container is closed, pointing up when open (following the Dashboard example).
    * Set a consistent color for open/close arrows.
    * Add a hover color for open/close arrows.
    * Make open/close arrows permanently visible; :hover states don't exist on touch.

    Make typography and color more consistent:

    * Switch all declarations of #dedede to #ddd for simplicity's sake.
    * Make H3s the same weight and color as H2s.
    * Make select elements the same color as text inputs.
    * Make paragraph text #444 so it doesn't look washed out on grey backgrounds.

    General improvements:

    * Eliminate the border between rows in settings tables.
    * Make text and buttons line up in the Customizer.

    See #25858.


    Built from https://develop.svn.wordpress.org/trunk@26293


    git-svn-id: http://core.svn.wordpress.org/trunk@26198 1a063a9b-81f0-0310-95a4-ce76da25c4cd