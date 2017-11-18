commit 3145ab8c1be61a63890b367f2a0e7d0056f2694f
Author: Gilles Debunne <debunne@google.com>
Date:   Tue Jan 11 14:35:39 2011 -0800

    SelectAllOnFocus shows a higlighted text. DO NOT MERGE.

    Bug 3201383

    Highlighted is different from selected, only the background is modified
    and selection mode is not started.

    Tapping inside a highlighted text places the cursor. This is especially
    useful for WebView and search bar has been modified to select all on focus.

    Selection handles time out is no longer needed.

    This CL is pretty involved and especially messes up with the terrible
    ExtractedTextView, which causes a lot of problem with text selection
    across device rotations.

    The current implementation works pretty well. It has one problem: the handles
    are not displayed when switching to landscape mode with a selected text.

    This is still an improvement over the current GB version, where the handles
    are not preserved at all across device rotation and where I can find more bugs.

    Handles are now hidden when a context menu is displayed.

    I can polish this more if we decide to include this in the MR1.

    Change-Id: Id10bf2808ff25752efd59a1987e91d609ba478cd