commit f63b89c0a10b4e3220b0a4aa1703be3aed0c5a98
Author: Fyodor Kupolov <fkupolov@google.com>
Date:   Tue Oct 27 18:08:56 2015 -0700

    UserController refactoring

    Addressed comments from the previous cl. Added getters for UserController
    fields, direct access is no longer allowed. Moved the following methods:
     - getUserManagerLocked. Became getUserManager() - because locking is not
       necessary, double checked locking will suffice.
     - startUserInForeground /showUserSwitchDialog/sendUserSwitchBroadcastsLocked
     - handleIncomingUser/unsafeConvertIncomingUserLocked/isUserRunningLocked/
       getUsers/getProfileIds

    Bug: 24745840
    Change-Id: Id5a5cfb9604e08add29bd9a03c8fe5200bc51fef