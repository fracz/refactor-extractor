commit 5b36a09e52ecefc0812f9762ba4d7e16d636a76b
Merge: 545c6f2 98a0052
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 18 10:32:20 2012 +0200

    merged branch lsmith77/router_debug_refactoring (PR #3963)

    Commits
    -------

    98a0052 improved readability
    b06537e refactored code to use get() when outputting a single route

    Discussion
    ----------

    Router debug refactoring

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: [![Build Status](https: //secure.travis-ci.org/lsmith77/symfony.png?branch=router_debug_refactoring)](http://travis-ci.org/lsmith77/symfony)
    Fixes the following tickets: -

    refactored code to use get() when outputting a single route
    this is useful for a CMS, where in most cases there will be too many routes to make it feasible to load all of them. here a router implementation will be used that will return an empty collection for ->all(). with this refactoring the given routes will not be listed via router:debug, but would still be shown when using router:debug [name]