commit 637c75bca176d3ef7a06e8b4fa2d60fece1c89a7
Author: Thomas Pulzer <t.pulzer@kniel.de>
Date:   Fri Jul 8 13:22:34 2016 +0200

    Implemented visual feedback if a user is disabled in admin user menu.

    Implemented visuals for enabling/disabling user from admin user list.
    Added the controller functions for enabling/disabling a user.

    Added the route for changing user status (enabled/disabled) and added an additional route handler in the user controller.
     Finished the visuals to reflect current user status and changed user status respectively.

    Changed the single icon for enabling/disabling a user into a menu where deletion and state toggling of a user is selectable.

    Added displaying of disabled user count.
    Improved style of user action menu.

    Added proper counting of disabled users.
    Removed visual indicator for disabled users.

    Moved pseudo-group detection for disabled users from frontend to the controller.
    Changed units for newly introduced css values from em to px.
    Removed unnecessary png and optimized svg with scour.
    Changed the userlist template to display the user action menu with correct width.

    Style fixes for better readability and coding style conformity.

    Changed the icons for enabling, disabling and deleting a user in the action menu.