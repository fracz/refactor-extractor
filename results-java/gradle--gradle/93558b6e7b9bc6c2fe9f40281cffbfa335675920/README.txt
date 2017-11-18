commit 93558b6e7b9bc6c2fe9f40281cffbfa335675920
Author: Cedric Champeau <cedric@gradle.com>
Date:   Mon Dec 5 11:52:10 2016 +0100

    Revalidate that selected configurations match the requested attributes

    This commit changes the behavior of dependency resolution in case a selected configuration doesn't match
    the requested attributes. It could happen in two cases:

    - in case no matching configuration is found, we fallback to the `default` configuration, which could have attributes
    that did *not* match (it was part of the selection, but in the end since no configuration was matching, it was selected
    anyway).
    - in case an explicit configuration was chosen. In that case, we didn't check that the selected configuration matched
    the consumer attributes.

    Error messages have been improved as part of this story. It's worth noting that this commit does NOT change the
    selection algorithm, and we will always fallback to the `default` configuration in case no match is found. The only
    thing it does is really revalidating that this fallback is compatible.

    See gradle/performance#233