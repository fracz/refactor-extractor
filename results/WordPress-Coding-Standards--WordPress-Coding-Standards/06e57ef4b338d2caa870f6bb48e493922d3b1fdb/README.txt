commit 06e57ef4b338d2caa870f6bb48e493922d3b1fdb
Author: jrfnl <jrfnl@users.noreply.github.com>
Date:   Wed Aug 2 17:50:37 2017 +0200

    PrefixAllGlobalsSniff: allow "prefix + non-word char" for improved support of hook names

    Words in hook names by default should be separated by underscores, however, this is checked by another sniff and not the concern of this sniff.
    As that sniff `ValidHookName` also allows for passing other separator characters, this sniff should allow for that.