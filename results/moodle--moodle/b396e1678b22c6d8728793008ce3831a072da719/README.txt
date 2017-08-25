commit b396e1678b22c6d8728793008ce3831a072da719
Author: kordan <kordan@mclink.it>
Date:   Mon May 23 15:05:03 2011 +0800

    MDL-27588 Fixed up several bugs with the formal_white theme

    -> MDL-26934 has been fixed
    -> MDL-27474 has been fixed
    -> one more option to have blocks column with different background colours
    -> one more option to use custom font size
    -> version.php, and b/install.php to handle upgrades
    -> increased the font size in the table with plugin list shown at upgrade time
    -> minor fixes in IE7 when images from remote web sites are shown in the header
    -> conformed the dock background colour to the block background colour to avoid unpleasant color combinations
    -> conformed a.link, a.visited and a.active colours in tabs
    -> conformed a.link, a.visited and a.active colours in the navigation block
    -> deleted an awful box border from enrolment page
    -> right aligned commands for resources and activities in the course page
    -> increased the width of field item title column in mform
    -> increased the padding-top to the help popup box messages
    -> user menu restyled to fix some minor tweaks in IE7
    -> minor repair to tabs to improve its look
    -> restyling of the docked item panel header to look like all the other blocks
    -> reduced the distance between elements in headermenu to look better even when only headings are shown in the header
    -> links wherever and always black
    -> the "search courses" field in the navigation bar does not force extra height
    -> some unpleasant generalbox border duplicating some other border have been removed
    -> some unpleasant margin/padding in notices and mtables has been removed