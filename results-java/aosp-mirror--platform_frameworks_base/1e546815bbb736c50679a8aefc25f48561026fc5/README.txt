commit 1e546815bbb736c50679a8aefc25f48561026fc5
Author: Victoria Lease <violets@google.com>
Date:   Tue Jun 25 14:25:17 2013 -0700

    Support RGBA fonts and bitmap fonts (and RGBA bitmap fonts)

    Quite a few things going on in this commit:

    - Enable bitmap strikes by default in Paint objects.

    The SkPaint parameter that enables bitmap strikes was not previously
    included in DEFAULT_PAINT_FLAGS. This effectively disabled bitmap
    fonts. Oops! It's for the best, though, as additional work was needed
    in Skia to make bitmap fonts work anyway.

    - Complain if TEXTURE_BORDER_SIZE is not 1.

    Our glyph cache code does not currently handle any value other than 1
    here, including zero. I've added a little C preprocessor check to
    prevent future engineers (including especially future-me) from
    thinking that they can change this value without updating the related
    code.

    - Add GL_RGBA support to hwui's FontRenderer and friends

    This also happened to involve some refactoring for convenience and
    cleanliness.

    Bug: 9577689
    Change-Id: I0abd1e5a0d6623106247fb6421787e2c2f2ea19c