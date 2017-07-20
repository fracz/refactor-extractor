commit 226a767be6122628fbc9e5e5f4da3e0e81a4a10f
Author: mattab <matthieu.aubry@gmail.com>
Date:   Tue Jun 3 13:37:07 2014 +1200

    Reuse the Settings object, for performance improvements (only parse the User agent once)
    refs https://github.com/piwik/piwik/pull/296