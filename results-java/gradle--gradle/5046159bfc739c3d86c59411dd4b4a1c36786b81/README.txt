commit 5046159bfc739c3d86c59411dd4b4a1c36786b81
Author: Cedric Champeau <cedric@gradle.com>
Date:   Fri Sep 8 18:12:18 2017 +0200

    Rename `VersionSelector` to `VersionMatcher`

    This refactor is introduced to reduce confusion: the selector corresponds to
    the requested version, which can be an exact version number, a range, or
    anything else. The matchers, on the other hand, match a selector. In other
    words, the selector is a specification, while the matcher does the actual
    work of matching.