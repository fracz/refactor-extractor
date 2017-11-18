commit b6d1fd9d222f324ef9fdee4ae106937653584d6b
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Tue Aug 1 22:52:18 2017 +0200

    Minor refactoring in PathPatternParser

    Remove the separator constructor argument (but preserve internal
    functionality) now that PathPatternParser is more explicitly purposed
    for URL paths and in any case the use of an alternate separator would
    also requires a similar input option on the PathContainer parsing side.