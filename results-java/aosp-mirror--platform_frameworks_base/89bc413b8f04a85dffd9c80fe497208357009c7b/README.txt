commit 89bc413b8f04a85dffd9c80fe497208357009c7b
Author: Michael Lekman <michael.lekman.x@sonyericsson.com>
Date:   Wed Jan 11 14:27:52 2012 +0100

    Marquee text RTL improvements

    Changed marquee text to scroll according to
    the reading direction. Arabic text will
    show the right edge and scroll towards
    the left edge and vice versa for Latin.

    Corrected marquee flicker when scroll animation
    finished. The ghost scroll's x position was cast
    to int and it made the text flicker when
    marquee stops.

    Ghost part didn't display for RTL languages.
    Added multiplication with
    getParagraphDirection to negate the ghost
    offset.

    Change-Id: I689039118df01a62f73ef0079c857fea1bfcc5a0