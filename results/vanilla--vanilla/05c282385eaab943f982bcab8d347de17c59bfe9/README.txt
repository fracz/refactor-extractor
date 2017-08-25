commit 05c282385eaab943f982bcab8d347de17c59bfe9
Author: Todd Burry <todd@vanillaforums.com>
Date:   Sat Feb 25 19:43:27 2017 -0500

    Bring Gdn_Session->getPermissions() back

    Backstory: We added the Permissions class a while back as a better way
    to wrangle permissions than just an opaque array. When doing so there
    was an incompatibility with Gdn_Session’s getPermissions() method.

    We could have come up with a new method, but not using the proper
    getter name for the permissions object kind of sucked. So we put a
    process in place:

    1. Add the getPermissionsArray() method and deprecate getPermissions()
    temporarily.
    2. Look through our source code for getPermissions() calls and migrate
    them to getPermissionsArray(). We’ll eventually want to refactor this
    too as manipulating the internal array should be a no-no.
    3. Re-introduce getPermissions() in the correct form (this PR).