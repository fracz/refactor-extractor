commit ae5b1bf59c8641d23cf93e211abaacfa372a6616
Author: Mark Story <mark@mark-story.com>
Date:   Sun Jan 31 23:17:22 2016 -0500

    Fix matchingData not having type information set.

    As part of the refactoring work done to make typecasting lazy and work
    with functions the casting around `_matchingData` was accidentally
    broken. This adds each attached association to the type map when fields
    are selected on a given association.

    Refs #8147