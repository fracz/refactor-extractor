commit 013afea33faddf8517763a1384d0898e34232ca1
Author: Cedric Champeau <cedric@gradle.com>
Date:   Fri Sep 8 18:12:18 2017 +0200

    Rename `VersionSelector` to `VersionMatcher`

    This refactor is introduced to reduce confusion: the selector corresponds to
    the requested version, which can be an exact version number, a range, or
    anything else. The matchers, on the other hand, match a selector. In other
    words, the selector is a specification, while the matcher does the actual
    work of matching.