commit 9c1e7f93d2afb45fbc2b0bb743c755866e8f11b0
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Apr 1 21:20:35 2015 +0300

    Reflection: refactor property construction and fix memory leak

    Properties obtained by KClass.properties were having strong references to
    descriptors (captured by closures, which are strongly retained by
    DescriptorBasedProperty). Support initial value in lazy soft properties