commit 5a92cd0b8dafb95de1fc862ed4446a8626a748f4
Author: David Mudrák <david@moodle.com>
Date:   Thu Oct 1 15:56:26 2015 +0200

    MDL-49329 admin: Display missing dependencies on plugins check screen

    The patch improves the dependencies resolution in the plugin manager so
    that the information about availability of the missing dependency is
    included and can be displayed at the Plugins check screen during the
    upgrade.