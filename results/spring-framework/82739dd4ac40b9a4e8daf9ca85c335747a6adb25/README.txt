commit 82739dd4ac40b9a4e8daf9ca85c335747a6adb25
Author: Arjen Poutsma <apoutsma@vmware.com>
Date:   Mon Sep 10 14:28:53 2012 +0200

    Refactor BeanInfoFactory

    This commit refactors the BeanInfoFactory so that:

     - supports() and getBeanInfo() are folded into one, so that getBeanInfo()
        returns null if a given class is not supported.
     - CachedIntrospectionResults now uses SpringFactoriesLoader