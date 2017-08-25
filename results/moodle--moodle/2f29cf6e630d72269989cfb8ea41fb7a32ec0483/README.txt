commit 2f29cf6e630d72269989cfb8ea41fb7a32ec0483
Author: David Mudrák <david@moodle.com>
Date:   Sat Oct 3 20:00:05 2015 +0200

    MDL-49329 admin: Add ability to cancel installation of a new plugin

    The plugins check screen now provides buttons to cancel installation of
    a plugin. Available only for new installations (not upgrades) and for
    additional plugins (not standard), given that the web server process has
    write access to the plugin folder.

    This has also been reported as MDL-48535.

    As a part of the patch, there is improved processing of page URLs during
    the upgrade. All this dancing around $reload URL is not needed once the
    $PAGE->url is properly set to guide the admin on the correct page during
    the upgrade process.