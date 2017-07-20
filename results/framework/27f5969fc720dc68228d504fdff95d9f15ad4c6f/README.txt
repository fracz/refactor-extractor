commit 27f5969fc720dc68228d504fdff95d9f15ad4c6f
Author: crynobone <crynobone@gmail.com>
Date:   Wed Jun 3 08:26:54 2015 +0800

    [5.1] Fews improvement to Routing component:

    * Avoid using last() since it's just an alias to end()
    * Refactor array_get() to just use isset($a) ? $a : null when possible/
    * Avoid using array_get() on middleware name to allow using '.' as route middleware name.

    Signed-off-by: crynobone <crynobone@gmail.com>