commit 528c64887e01dd6a9d802d657838499b9fac0cb2
Author: Gilles Debunne <debunne@google.com>
Date:   Fri Oct 8 11:56:13 2010 -0700

    TextView cursor and selection improvements.

    Insertion cursor handle no longer appears on empty text views (Bug 3075988).

    Tapping on an unfocused TextView moves the insertion point at tapped position.

    Bug fixes for trackball initiated text selection.

    Change-Id: Ief246fd9a9f1eb745dcf9f0605e2ce53b5563f01