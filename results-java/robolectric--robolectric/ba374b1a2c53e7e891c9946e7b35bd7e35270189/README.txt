commit ba374b1a2c53e7e891c9946e7b35bd7e35270189
Author: Jonathan Gerrish <jonathan@indiekid.org>
Date:   Wed Jun 14 14:08:46 2017 -0700

    PackageManager.getProviderInfo() improvements. (#3191)

    This method should throw an exception when the package name cannot be
    resolved as per the Javadocs:-

    https://developer.android.com/reference/android/content/pm/PackageManager.html#getProviderInfo(android.content.ComponentName,
    int)