commit 087e55cc510a535e661b12c5c1488f1684919da1
Author: Jason Tedor <jason@tedor.me>
Date:   Fri Jan 22 06:50:28 2016 -0500

    Script mode settings

    This commit converts the script mode settings to the new settings
    infrastructure. This is a major refactoring of the handling of script
    mode settings. This refactoring is necessary because these settings are
    determined at runtime based on the registered script engines and the
    registered script contexts.