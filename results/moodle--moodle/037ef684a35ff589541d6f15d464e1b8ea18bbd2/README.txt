commit 037ef684a35ff589541d6f15d464e1b8ea18bbd2
Author: Marina Glancy <marina@moodle.com>
Date:   Tue Jan 10 14:39:06 2012 +0800

    MDL-31072: Fixed bug causing memory overflow for many-user systems:

    when function find_users was supposed to be used for user validation but no userids were passed to it, it retrieved all users in the system.
    This caused memory overflow on systems with too many users (like moodle.org).
    Now we make sure that find_users is not called for validation if there is nobody to validate.

    Also improved query inside find_users to make it work faster