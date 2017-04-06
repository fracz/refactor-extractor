commit c2e45c769ef7a45f2bd14870236fd8e8bed38246
Author: sunnylost <sunnylost@gmail.com>
Date:   Mon Oct 14 15:03:11 2013 +0800

    refactor(angular.js): improve trim performance

    According to Flagrant Badassery's blog
    http://blog.stevenlevithan.com/archives/faster-trim-javascript
    and this comparison http://jsperf.com/trim-function, this trim method is faster.

    Closes #4406