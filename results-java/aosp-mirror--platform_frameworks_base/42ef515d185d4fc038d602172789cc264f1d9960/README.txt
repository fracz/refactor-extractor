commit 42ef515d185d4fc038d602172789cc264f1d9960
Author: Raph Levien <raph@google.com>
Date:   Mon Oct 22 15:01:17 2012 -0700

    Fix for bug: Gmail (and other places): cursor placed on top of letter

    This patch fixes bug 7346656. In this particular case, the text line in
    the EditText was split into multiple spans, with the boundary between
    the "r" and "," in "r,". These were being drawn as two separate runs,
    but measured as a single run, leading to inconsistent measurements
    because this is a kern pair in Roboto.

    The fix is to eliminate the special-case code for measuring. This will
    actually improve efficiency, as the value computed in one pass is now
    more likely to be reused in another.

    Change-Id: I04142a0ec98f280fc1027c7cbdbf903e3096f8e4