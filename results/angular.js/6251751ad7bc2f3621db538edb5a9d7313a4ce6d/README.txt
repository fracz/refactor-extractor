commit 6251751ad7bc2f3621db538edb5a9d7313a4ce6d
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Aug 4 23:32:03 2014 -0700

    perf(jqLite): don't register DOM listener for $destroy event

    This even is fired purely within jqLite/jQuery so it doesn't make sense to register DOM listener here.

    6% improvement in large table benchmark for both creation and destruction