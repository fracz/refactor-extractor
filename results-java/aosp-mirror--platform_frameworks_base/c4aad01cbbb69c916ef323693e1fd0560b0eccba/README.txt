commit c4aad01cbbb69c916ef323693e1fd0560b0eccba
Author: Dianne Hackborn <hackbod@google.com>
Date:   Fri Feb 22 15:05:25 2013 -0800

    Formalize overscan metrics.

    The window manager now maintains and reports a new formal
    "overscan insets" for each window, much like the existing
    content and visible insets.  This is used to correctly
    position the various UI elements in the various combination
    of layout options.  In particular, this allows us to have
    an activity that is using fitSystemWindows to have the content
    of its UI extend out to the visible content part of the screen
    while still positioning its fixed UI elements inside the
    standard content rect (and the entire window extending all
    the way into the overscan area to fill the screen as desired).

    Okay, maybe that is not written so clearly.  Well, it made
    my head hurt too, so suffer!

    The key thing is that windows now need to know about three
    rectangles: the overall rectangle of the window, the rectangle
    inside of the overscan area, and the rectangle inside of the
    content area.  The FLAG_LAYOUT_IN_OVERSCAN option controls
    whether the second rectangle is pushed out to fill the entire
    overscan area.

    Also did some improvements to debug dumping in the window
    manager.

    Change-Id: Ib2368c4aff5709d00662c799507c37b6826929fd