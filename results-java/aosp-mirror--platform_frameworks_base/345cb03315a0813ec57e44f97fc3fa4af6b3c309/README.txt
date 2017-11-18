commit 345cb03315a0813ec57e44f97fc3fa4af6b3c309
Author: Gilles Debunne <debunne@google.com>
Date:   Wed Jun 16 17:13:23 2010 -0700

    Index out of range problem in TextLine.

    Recent refactoring for bidi introduced an index shift in the getOffsetBeforeAfter
    method. This problem appears for multi-line text input only, when the text line
    mStart index is not 0.

    As a result, moving the cursor using the trackball in a multi-line EditText crashes.

    Change-Id: I1f121f0f9272ef7d338399f369ba6d77e1ca71c5