commit 04468db44185e3d7968abdb23d77bf623cb5021b
Author: Igor Minar <igor@angularjs.org>
Date:   Wed Jun 4 07:20:19 2014 -0700

    perf(shallowCopy): use Object.keys to improve performance

    This change is not IE8 friendly