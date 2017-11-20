commit e388f6d15ce0ab157aa96245fb5ed2834435c52a
Author: nitsanw <nitsanw@yahoo.com>
Date:   Mon May 22 21:27:04 2017 +0200

    [Java] Add response code error type and introduce some error types.

    - use boolean return for id lookup fail on replay/record abort
    - abort replay id was not wired all the way
    - refactor session state validation to reduce duplication