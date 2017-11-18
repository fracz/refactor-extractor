commit 46ac6fa614131d567bed93d1d2067d765ecef85d
Author: Craig Mautner <cmautner@google.com>
Date:   Thu Aug 1 10:06:34 2013 -0700

    Add force default orientation.

    Devices can be configured to remain in their default landscape or
    portrait orientation by setting config_forceDefaultOrientation true
    in overlay/.../values/config.xml.

    Activities that desire to run in the non-default orientation are
    supported by creating a logical display within the physical display.
    Transitions to and from the activity perform a crossfade rather than
    the normal rotation animation.

    Also, improve SurfaceTrace debug output.

    Fixes bug 9695710.

    Change-Id: I053e136cd2b9ae200028595f245b6ada5927cfe9