commit 97a97f9bba284210ac372b045c1ea7a5c4b1fae8
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Wed Jun 14 15:58:44 2017 -0400

    RequestPath improvements

    Static parse methods on PathSegmentContainer and PathSegment plus:

    isEmpty() on PathSegmentContainer and PathSegment
    isAbsolute() and hasTrailingSlash() on PathSegmentContainer
    char[] alternative for valueDecoded() on PathSegment