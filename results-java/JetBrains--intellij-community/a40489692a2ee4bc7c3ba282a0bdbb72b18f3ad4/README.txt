commit a40489692a2ee4bc7c3ba282a0bdbb72b18f3ad4
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Fri Aug 16 14:57:03 2013 +0400

    Add new version of GenericRepository. This commit consolidates several related changes:

    * selector based task information extraction from server responses instead of previous RegExp approach (though it's still supported)
    * improved representation of template variables  in configuration dialog
    * new "repository subtype" conception which allows to create new connectors based on Generic and stored as simple configuration files
    * connector for Asana collaborative task service based created this way