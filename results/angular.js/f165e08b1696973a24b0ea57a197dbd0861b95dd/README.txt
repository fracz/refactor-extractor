commit f165e08b1696973a24b0ea57a197dbd0861b95dd
Author: Pavel Pomerantsev <pomerantsevp@gmail.com>
Date:   Sat May 31 14:10:07 2014 +0400

    refactor(bootstrap): remove an unused argument

    The $animate service is injected, but not used within the code anymore.

    Closes #7649