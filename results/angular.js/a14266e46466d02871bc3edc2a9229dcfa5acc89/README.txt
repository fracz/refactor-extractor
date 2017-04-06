commit a14266e46466d02871bc3edc2a9229dcfa5acc89
Author: Matias Niemelä <matias@yearofmoo.com>
Date:   Fri Dec 6 12:54:20 2013 -0500

    chore(CHANGELOG): introduce perf() section for performance-related commits

    Instead of using fix() or chore() when labelling a commit which improves
    speed or performance use perf(). Perf commits will be listed in the
    CHANGELOG under "Performance Improvements".

    For example:
    perf($animate): cache all getComputedStyle operations to reduce additional reflows