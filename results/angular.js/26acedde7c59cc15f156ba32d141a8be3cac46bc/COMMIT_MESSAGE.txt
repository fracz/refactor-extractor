commit 26acedde7c59cc15f156ba32d141a8be3cac46bc
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Fri Jul 15 15:23:57 2016 +0300

    refactor(test/e2e): remove explicit call to `waitForAngular()`

    Protractor automatically calls `waitForAngular()`, before every WebDriver action.

    Closes #14920