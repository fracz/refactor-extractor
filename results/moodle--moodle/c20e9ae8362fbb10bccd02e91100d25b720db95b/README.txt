commit c20e9ae8362fbb10bccd02e91100d25b720db95b
Author: David Mudrák <david@moodle.com>
Date:   Fri Oct 9 18:07:28 2015 +0200

    MDL-49329 admin: Add ability to cancel upgrade of the plugin

    If there is an available archived zip with the version of the plugin
    currently installed, we can use it to cancel/abort the upgrade of the
    plugin. This is internally handled as the installation of the archived
    zip and goes through all the validation and confirmation.

    Additionally, some other parts were improved. Most notably, renderer no
    longer decides itself if some installation can be cancelled but it
    always asks the controller (plugin manager).

    The button for installation was moved to the left so there should be
    first buttons to add things, and then buttons to cancel things (which is
    common in normal forms).