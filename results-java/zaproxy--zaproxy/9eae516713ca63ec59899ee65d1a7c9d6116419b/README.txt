commit 9eae516713ca63ec59899ee65d1a7c9d6116419b
Author: thc202 <thc202@gmail.com>
Date:   Mon Dec 21 08:54:53 2015 +0000

    Do not share Plugin instances between PluginFactory instances

    Change method PluginFactory.loadAllPlugin(Configuration) to create new
    Plugin instances instead of using the same for all PluginFactory
    instances already "loaded", otherwise the (shared) Plugin instances
    would have the configurations of the latest ScanPolicy loaded. The
    instantiation of the Plugin is only done if the Plugin can be used (that
    is, is visible, non deprecated and not an older unstable version).
    Add auxiliary method, createNewPlugin(Plugin, Configuration), that
    creates the new Plugin instance (and loads the configurations from the
    configuration file of the ScanPolicy).
    Add tests to assert the expected behaviour (and refactor Plugin helper
    test code to a new class).
    Fix #2112 - Wrong policy on active Scan