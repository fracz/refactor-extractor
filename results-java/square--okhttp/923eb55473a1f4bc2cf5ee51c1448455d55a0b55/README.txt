commit 923eb55473a1f4bc2cf5ee51c1448455d55a0b55
Author: jwilson <jwilson@squareup.com>
Date:   Tue Apr 9 12:53:51 2013 -0700

    Be more strict about outgoing request headers.

    We now forbid empty header names, and '\0' characters in names
    or values. This should improve the experience for SPDY users,
    who will see the failure when they add the header rather than
    when the request is made.