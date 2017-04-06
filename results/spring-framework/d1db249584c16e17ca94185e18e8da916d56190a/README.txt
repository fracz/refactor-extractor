commit d1db249584c16e17ca94185e18e8da916d56190a
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Wed Mar 29 16:10:53 2017 -0400

    Fix regression in client codecs

    Restore the correct client-side default for whether StringDecoder
    should split on new lines. It is true forthe server and false for the
    client by default.

    The regression was introduced in the recent refactoring:
    https://github.com/spring-projects/spring-framework/commit/f8a21ab11bf1ecad7ac7866f280d42c70be48b8a#diff-0175d58138b2e8b2bec087ffe0495340