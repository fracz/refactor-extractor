commit 5df7e73adf56ff4b776232fdb631e0f7d004864f
Author: Gias Kay Lee <balancetraveller+github@gmail.com>
Date:   Tue Jan 7 21:25:46 2014 +0800

    refactor(booleanAttrs, ngSwitch): use link function instead of compile function where appropriate

    Replace two compile functions that immediately return a post-link function with link function definitions instead.

    Closes #5664