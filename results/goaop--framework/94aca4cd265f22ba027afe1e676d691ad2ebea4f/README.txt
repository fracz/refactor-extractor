commit 94aca4cd265f22ba027afe1e676d691ad2ebea4f
Author: Alexander lisachenko <lisachenko.it@gmail.com>
Date:   Tue Aug 26 17:37:17 2014 +0300

    BACKPORTED: Change the logic of checking for dynamic pointcuts

    This fix allows to improve the checking for dynamic pointcuts by
    supporting logical operators. However, logical pointcuts should also support multiple
    arguments for dynamic matching via matches($point, $instance, $args)

    Conflicts:
            src/Aop/Support/DynamicMethodMatcher.php
            src/Core/GeneralAspectLoaderExtension.php