commit e708744a907a2fb52d399df98a481ff8392f6785
Author: thc202 <thc202@gmail.com>
Date:   Mon Nov 16 17:30:23 2015 +0000

    Fix issues with installation of passive and active scanners

    Change to load and set the quality of the passive and active scanners
    during installation of the add-on, preventing the quality from being
    "Unknown". The loading/instantiation of the scanners is now done by the
    same code (refactored to new class AddOnLoaderUtils, also refactored the
    code that loads/instantiates a class, or classes, from an add-on). The
    methods previously used during the installation, ExtensionPassiveScan.
    addPassiveScanner(String) and PluginFactory.loadedPlugin(String), are
    now deprecated as the quality is not correctly set. The method
    PluginFactory.unloadedPlugin(String) is also deprecated, no longer used
    by core code (the active scanners are now removed using the loaded
    instances).
    Change to load the active scanners just once by checking that the
    scanners were not already loaded/added. The active scanners were being
    added twice because the method PluginFactory.loadedPlugin(String) called
    the method that initialised the list of scanners which was being
    initialised with the add-ons already loaded, which included the add-on
    being installed. The PluginFactory now checks if a scanner is already
    loaded by reference and when removing, to make sure that's checking and
    removing the exact instance (added). Previously it was using the equals
    of the default implementation (AbstractPlugin) which checked the
    equality by using the ID of the scanner, which would lead to unexpected
    results when different scanners have (wrongly) the same ID (as it could
    remove the other scanner).
    The AddOn now keeps references to the scanners loaded, which is used to
    prevent loading the scanners more than once and are used during the
    uninstallation of the add-on.
    Fix #1969 - Issues with installation of scanners