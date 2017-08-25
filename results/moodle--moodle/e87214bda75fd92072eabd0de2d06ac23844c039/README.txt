commit e87214bda75fd92072eabd0de2d06ac23844c039
Author: Petr Škoda <commits@skodak.org>
Date:   Fri Oct 4 22:40:44 2013 +0200

    MDL-42078 multiple uninstall improvements and cleanup

    Includes:
    * update checker refactored to \core\update\ namespace
    * plugininfo classes refactored to \core\plugininfo\ namespace
    * plugin_manager renamed to core_plugin_manager
    * redirect back to original page after plugin uninstall
    * fixed assign subplugin uninstall
    * move assign subplugins under the assignment in admin tree
    * fixed plugininfo for all question related plugin types
    * auth uninstall support
    * added missing block dependencies
    * added theme uninstall
    * subplugin types are following the plugin on plugin overview page
    * several performance improvements in plugin manager
    * new warnigns when plugininfo are outdated or missing
    * multiple fixes and other improvements