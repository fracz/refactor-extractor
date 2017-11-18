commit 650de3dcfcbc7635da3c070410ef1dc4027ae464
Author: Jeff Brown <jeffbrown@google.com>
Date:   Thu Oct 27 14:52:28 2011 -0700

    Optimize fillWindow to improve reverse-seek performance.
    Bug: 5520301

    When an application requests a row from a SQLiteCursor that
    is not in the window, instead of filling from the requested
    row position onwards, fill from a little bit ahead of the
    requested row position.

    This fixes a problem with applications that seek backwards
    in large cursor windows.  Previously the application could
    end up refilling the window every time it moved back
    one position.

    We try to fill about 1/3 before the requested position and
    2/3 after which substantially improves scrolling responsiveness
    when the list is bound to a data set that does not fit
    entirely within one cursor window.

    Change-Id: I168ff1d3aed1a41ac96267be34a026c108590e52