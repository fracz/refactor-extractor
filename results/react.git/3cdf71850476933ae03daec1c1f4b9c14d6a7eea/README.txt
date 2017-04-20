commit 3cdf71850476933ae03daec1c1f4b9c14d6a7eea
Author: Sebastian Markbage <sema@fb.com>
Date:   Wed Jun 10 17:34:16 2015 -0700

    Use the public render API in ReactDOMComponent-test

    Avoids testing a non-public API. First step towards refactoring more of
    these internals to not be instances. Also gets rid of an _owner usage.