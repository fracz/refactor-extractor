commit c6d1c345f41cf817bf2c07c97b97107d94296064
Author: Svetoslav <svetoslavganov@google.com>
Date:   Thu Feb 26 14:44:43 2015 -0800

    Runtime permissions: per user permission tracking.

    Before all permissions were granted at install time at once, so the user
    was persented with an all or nothing choice. In the new runtime permissions
    model all dangarous permissions (nomal are always granted and signature
    one are granted if signatures match) are not granted at install time and
    the app can request them as necessary at runtime.

    Before, all granted permission to an app were identical for all users as
    granting is performed at install time. However, the new runtime model
    allows the same app running under two different users to have different
    runtime permission grants. This change refactors the permissions book
    keeping in the package manager to enable per user permission tracking.

    The change also adds the app facing APIs for requesting runtime permissions.

    Change-Id: Icbf2fc2ced15c42ca206c335996206bd1a4a4be5