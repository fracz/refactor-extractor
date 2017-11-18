commit c99d33fb4dcf332b66281a0a6a31710407fc829d
Author: Jean Chalard <jchalard@google.com>
Date:   Thu Feb 28 16:39:47 2013 -0800

    Actually change the place where updateSelection is called

    Call updateSelection in endBatchEdit instead of onDraw.
    This works because all edits go through a batch edit,
    which is already the case although the next change will
    enforce it going forward.
    This is step 3 of a four-step refactoring.

    Bug: 8000119
    Change-Id: Ia5e257382e2ef2168726bf3d9c7c84f9379ba376