commit d208ba254442649d35f96c76bcd9e47326ec59f3
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Aug 11 11:26:53 2014 -0700

    perf(isObject): use strict comparison

    this is a micro-optimization based on http://jsperf.com/isobject4

    no significant improvement in macro-benchmarks, but since it makes the code better it makes
    sense making this change.