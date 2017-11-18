commit f02780d30faded0c52343f39077bd962d9efbf96
Author: Greg Leclercq <ggreg@fb.com>
Date:   Thu Dec 22 21:43:02 2016 -0800

    Fix and refactor boolean expression optimization

    This fixes a correctness issue in the boolean expression optimization that
    occurs when a multiple nested boolean expression attempts to factor out some
    common sub-expressions.

    For Example:
    ((A AND B) OR (A AND C)) AND D