commit 3337dbf707b0c4fba34fe4ce7b36cfe1c474e02c
Author: Nick Kralevich <nnk@google.com>
Date:   Mon Apr 1 13:27:30 2013 -0700

    grantPermissionsLPw: introduce isNewPlatformPermissionForPackage

    Make grantPermissionsLPw by refactoring some code into a new
    function, isNewPlatformPermissionForPackage.

    No functional changes.

    Change-Id: I467dacfe1fcf7e77cef4cb6df54536eeaafd9064