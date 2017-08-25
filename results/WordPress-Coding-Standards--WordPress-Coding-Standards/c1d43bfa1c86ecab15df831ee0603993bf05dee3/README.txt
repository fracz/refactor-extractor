commit c1d43bfa1c86ecab15df831ee0603993bf05dee3
Author: jrfnl <jrfnl@users.noreply.github.com>
Date:   Mon Aug 7 17:24:33 2017 +0200

    XSS.EscapeOutput sniff: Fix issue #933 - namespace separators.

    This simple change means that namespace separators will be be ignored completely by the check for output escaping which fixes the immediate issue.

    For a more thorough fix, the logic of the function would need to be refactored to take namespaced functions into account as well, but that's for another day.