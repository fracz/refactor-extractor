commit d746e3fbfaee246055f1629d53513879e41540c0
Author: Brian Clozel <bclozel@gopivotal.com>
Date:   Wed Jun 11 20:43:08 2014 +0200

    Rollback AntPathMatcher behavior for ".*" comparisons

    Prior to this commit, AntPathMatcher had been refactored for SPR-6741.
    During that process, a key feature has been removed:
    When comparing two patterns, pattern elements (*, {}, etc) are counted
    to score those patterns. When a pattern ends with ".*", the ending
    wildcard should not be counted against pattern elements for this
    pattern.

    This commit reintroduces that behavior.

    Issue: SPR-6741