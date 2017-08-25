commit 3ca1b54642568a42c663f77af8940a59ae5c1229
Author: David Mudrák <david@moodle.com>
Date:   Fri Apr 12 04:02:28 2013 +0200

    MDL-39087 Use progress_trace class to display uninstallation progress

    This is much better API than using the array passed by reference. At the
    moment, it is pretty hacky as it abuses text_progress_trace to output
    raw HTML echoed by uninstall_plugin() but that will be improved later
    while moving the logic out of that function into the plugin_manager.