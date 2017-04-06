commit 5b5f35d5e41a5930f7a685a02d0d20b8585a1420
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Jan 8 14:23:50 2013 -0800

    refactor($browser): remove faulty 20+ cookies warning

    the warning is defunct (and the test is incorrect) so obviously nobody is using
    it and it just takes up space.

    also the browser behavior varies (ff and chrome allow up to 150 cookies, safari
    even more), so it's not very useful.

    Closes #1712