commit f3999ef465fbc94a783c1d8efabae6782f948e30
Author: Todd Burry <todd@vanillaforums.com>
Date:   Fri Jun 27 14:25:16 2014 -0400

    PermissionModel refactor.

    - Allow GetGlobalPermissions() to get an array of roles rather than just one.
    - Add a check to the schema in Save() so that passing too many columns does not result in an error.