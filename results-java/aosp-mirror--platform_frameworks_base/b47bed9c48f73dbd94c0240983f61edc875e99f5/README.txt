commit b47bed9c48f73dbd94c0240983f61edc875e99f5
Author: Jim Miller <jaggies@google.com>
Date:   Sun Jan 16 14:52:53 2011 -0800

    Fix 3355957: Don't hide the keyboard for PIN/Password

    BT or USB keyboards currently cause the on-screen keyboard to
    be hidden.  This will probably need to be refactored in the
    future but the change is too complicated for the current
    release.

    Change-Id: Ieb655b85622f7c174ed4a5d1c3766d130a9d32f1