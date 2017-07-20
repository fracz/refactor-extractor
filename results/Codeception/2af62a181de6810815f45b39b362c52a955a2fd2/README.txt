commit 2af62a181de6810815f45b39b362c52a955a2fd2
Author: Zaahid Bateson <zbateson@mail.ubc.ca>
Date:   Wed Apr 8 10:32:11 2015 -0700

    Fix for multi-dimensional form array values

    Fixes an issue setting form names to values like field[name] and
    refactored to use DomCrawler Form's getPhpValues.