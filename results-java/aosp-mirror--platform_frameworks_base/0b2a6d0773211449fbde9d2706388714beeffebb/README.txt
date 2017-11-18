commit 0b2a6d0773211449fbde9d2706388714beeffebb
Author: Jim Miller <jaggies@google.com>
Date:   Tue Jul 13 18:01:29 2010 -0700

    Fix 2797185: Re-enable thumbnail generation in framework

    This re-enables thumbnail generation code in the framework
    with a few improvements.

    In addition to enabling the system to capture thumbnails,
    it removes padding from the borders to account for space
    overlapped by system widgets (status bar, etc.). Thus,
    the contents of the bitmap are only those pixels unique to
    the activity.

    It also maximizes resolution of the bitmap by capturing the
    image in the application's current orientation. In landscape
    mode, it captures a bitmap with dimensions w x h.  In portrait,
    it captures a bitmap with dimensions h x w. Where w and h are
    thumbnail_width and thumbnail_height as defined in dimens.xml.

    Though enabled, the change is not currently used in this
    branch.  The work is being checked in here to avoid
    complicated downstream merges.

    Change-Id: Ifc8a4e0075d7d0697d8159589be3816ace31d70c