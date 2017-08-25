commit aa1d100c2d694b979b513de0ede700d9b6fef610
Author: David Mudrák <david@moodle.com>
Date:   Sat Oct 10 00:24:59 2015 +0200

    MDL-49329 admin: Fix checking for available updates

    This was a regression of my recent improvement of rendering the "Check
    for updates" button. There is now unified parameter ?fetchupdates=1 that
    can be used on either admin/index.php or admin/plugins.php, so that we
    can use a common UI widget for both locations (without the need to pass
    the URL explicitly).