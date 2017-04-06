commit 73a447da861d2413269064899443b297b9ca3f88
Author: Shay Banon <kimchy@gmail.com>
Date:   Sat Feb 16 02:25:04 2013 +0100

    initial facet refactoring
    the main goal of the facet refactoring is to allow for two modes of facet execution, collector based, that get callbacks as hist match, and post based, which iterates over all the relevant hits
    it also includes a some simplification of the facet implementation