commit f7cf846045b1e2fb39c62e304c61b44d5c805e31
Author: Georgios Kalpakas <g.kalpakas@hotmail.com>
Date:   Thu Oct 23 12:59:22 2014 +0300

    fix(filterFilter): correctly handle deep expression objects

    Previously, trying to use a deep expression object (i.e. an object whose
    properties can be objects themselves) did not work correctly.
    This commit refactors `filterFilter`, making it simpler and adding support
    for filtering collections of arbitrarily deep objects.

    Closes #7323
    Closes #9698
    Closes #9757