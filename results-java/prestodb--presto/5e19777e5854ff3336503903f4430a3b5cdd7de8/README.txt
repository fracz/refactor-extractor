commit 5e19777e5854ff3336503903f4430a3b5cdd7de8
Author: Christopher Berner <cberner@fb.com>
Date:   Mon Jul 28 12:28:51 2014 -0700

    Improve aggregation compilers

    Generate byte code for entire Accumulator classes, also refactor the
    verification logic to separate it from the compilation