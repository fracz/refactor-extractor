commit f11e9b973ccf376f22d6ae55880e75d241660c78
Author: Jonathan Gerrish <jonathan@indiekid.org>
Date:   Wed May 31 12:35:46 2017 -0700

    PackageManager simulation improvement. (#3133)

    Add support for PackageManager.getResourcesForApplication() - calling
    ShadowPackageManager.addPackage(...) now allows
    getResourcesForApplication() to work for that added package.

    Currently returns an empty resources object, could be extended in the
    future to load resources from ApplicationInfo.resDir