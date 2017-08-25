commit 507a7a9a2d73b7aa316625065e133e64b51d26e5
Author: Sam Hemelryk <sam@moodle.com>
Date:   Thu Feb 18 05:57:20 2010 +0000

    navigation MDL-21604 Fixed AJAX security issue + the following changes
    * AJAX expansion now uses JSON rather than XML
    * Tidied up unused globals in navigaitonlib after recent refactoring
    * Cleaned up Database calls that were no longer required after security changes