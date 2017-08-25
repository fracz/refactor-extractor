commit e51b0ac4afd12a175254f720283c6a946888a182
Author: uwetews <uwe.tews@googlemail.com>
Date:   Sat May 27 11:04:00 2017 +0200

    - performance store flag for already required shared plugin functions in static variable or
        Smarty's $_cache to improve performance when plugins are often called
        https://github.com/smarty-php/smarty/commit/51e0d5cd405d764a4ea257d1bac1fb1205f74528#commitcomment-22280086