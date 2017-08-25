commit 86a862cdc232d982953c1dafa8163f59e51d0673
Author: David Mudrák <david@moodle.com>
Date:   Thu Apr 11 14:24:41 2013 +0200

    MDL-39087 Add missing unit tests for the plugin_manager

    This patch improves and adds unit tests for the plugin_manager class.
    These unit tests cover the existing functionalities. Tests for the
    new features related directly with MDL-38259 will be added in a separate
    commit (to make it clear what's related to it).