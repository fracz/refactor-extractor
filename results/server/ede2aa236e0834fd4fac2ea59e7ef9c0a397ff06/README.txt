commit ede2aa236e0834fd4fac2ea59e7ef9c0a397ff06
Author: macjohnny <estebanmarin@gmx.ch>
Date:   Thu Jun 12 09:41:23 2014 +0200

    Update manager.php

    add a function getUserGroupIds for retrieving group ids instead of group objects. this significantly improves performance when using many (nested) groups.