commit 44c330aa5d392e91d4261c6f2e142932a2a26884
Author: Robin McCorkell <rmccorkell@karoshi.org.uk>
Date:   Tue Jan 20 12:40:18 2015 +0000

    Performance improvements for NaturalSort

    A combination of using isset() instead of count() or strlen(), caching the
    chunkify function, and replacing is_numeric() with some comparisons