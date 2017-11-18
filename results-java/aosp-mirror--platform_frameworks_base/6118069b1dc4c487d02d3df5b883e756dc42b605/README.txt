commit 6118069b1dc4c487d02d3df5b883e756dc42b605
Author: Irfan Sheriff <isheriff@google.com>
Date:   Wed Aug 18 16:07:39 2010 -0700

    Fix WRITE_SECURE_SETTINGS permission issue

    The refactor with the new state machine had introduced
    a bug with writes to secure settings in public API for
    which apps might not have permission.

    Bug: 2895750
    Change-Id: I7d236253201a47b836996859aa3de2806ad8a800