commit 4849a5ba3f9580fab14b1f095603983f6c51ae24
Author: gwoo <gwoohoo@gmail.com>
Date:   Sun Sep 18 06:35:23 2011 -0700

    BREAK: refactoring `Socket` and `Service` classes so connections are more configurable.
    Removed `Service::connection()` method. Added `Service::_init()` for initializng the `Socket` connection.
    Added magic method to handle any HTTP method.