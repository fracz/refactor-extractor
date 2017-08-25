commit bde002b81ac2fe003cc2b1e1db6e90c231baf1fa
Author: Petr Škoda <commits@skodak.org>
Date:   Sat Sep 14 23:57:21 2013 +0200

    MDL-41437 rework plugin_manager caching and version info in blocks and modules

    This patch includes:

    * version column removed from modules table, now using standard config, this allows decimal version for modules
    * version column removed from block table, now using standard config, this allows decimal version for blocks
    * module version.php can safely use $plugins instead of module
    * new plugin_manager bulk caching, this should help with MUC performance when logged in as admin
    * all missing plugins are now in plugin overview (previously only blocks and modules)
    * simplified code and improved coding style
    * reworked plugin_manager unit tests - now using real plugins instead of mocks
    * unit tests now fail if any plugin does not contain proper version.php file
    * allow uninstall of deleted filters