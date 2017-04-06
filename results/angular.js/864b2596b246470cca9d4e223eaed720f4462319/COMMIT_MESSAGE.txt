commit 864b2596b246470cca9d4e223eaed720f4462319
Author: Karl Seamon <bannanafiend@gmail.com>
Date:   Thu Dec 5 18:08:52 2013 -0500

    perf($parse) use a faster path when the number of path parts is low

    Use a faster path when the number of path tokens is low (ie the common case).
    This results in a better than 19x improvement in the time spent in $parse and
    produces output that is about the same speed in chrome and substantially faster
    in firefox.
    http://jsperf.com/angularjs-parse-getter/6

    Closes #5359