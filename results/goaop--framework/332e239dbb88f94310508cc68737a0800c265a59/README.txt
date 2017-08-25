commit 332e239dbb88f94310508cc68737a0800c265a59
Author: Alexander lisachenko <lisachenko.it@gmail.com>
Date:   Tue Aug 26 17:37:17 2014 +0300

    Change the logic of checking for dynamic pointcuts

    This fix allows to improve the checking for dynamic pointcuts by
    supporting logical operators. However, logical pointcuts should also support multiple
    arguments for dynamic matching via matches($point, $instance, $args)