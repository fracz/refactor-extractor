commit b5714ce1dfca743733e84431161fc888fdcad24a
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Aug 12 23:46:25 2014 -0700

    refactor(ngRepeat): simplify previousNode boundary calculation

    the previousNode was almost always correct except when we added a new block in which case incorrectly
    assigned the cloned collection to the variable instead of the end comment node.