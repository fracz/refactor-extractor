commit fb23c67e8a0083ee376ad3f0e98f97d06755eb57
Author: destradaa <destradaa@google.com>
Date:   Thu Apr 16 14:01:27 2015 -0700

    Update GpsLocationProvider construction sequence to save time on startup.
    b/20127262

    This change moves long running operations: such as IO and broadcast receivers registration
    to its internal handler. This sets the time to execute GpsLocationProvider ctor() to ~14ms.
    A significant improvement from the 285ms reported originally in the bug.

    Change-Id: I4ee4183a1afea86117004c0a052957b8bc1e4ce5