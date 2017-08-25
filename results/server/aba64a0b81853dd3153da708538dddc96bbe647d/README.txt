commit aba64a0b81853dd3153da708538dddc96bbe647d
Author: Markus Goetz <markus@woboq.com>
Date:   Sat Aug 17 16:01:37 2013 +0200

    Class Auto Loader: Cache paths in APC

    Using benchmark_single.php (from administration repo) I can
    measure a speed improvement of 5% to 20% loading the /index.php
    when logged in. (when using APC and php-fpm).