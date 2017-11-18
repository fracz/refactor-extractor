commit 35bee33d6f4fad8c1b1bc952ea0eeea22c4c7564
Author: Nick Kralevich <nnk@google.com>
Date:   Mon Apr 1 13:08:00 2013 -0700

    grantPermissionsLPw: introduce doSignaturePermission

    Make grantPermissionsLPw smaller by introducing a new doSignaturePermission
    function.

    Just a refactoring. No functional code changes.

    Change-Id: Ia967fd93e3f7cf3e48fcd13be0b04994b76d36f3