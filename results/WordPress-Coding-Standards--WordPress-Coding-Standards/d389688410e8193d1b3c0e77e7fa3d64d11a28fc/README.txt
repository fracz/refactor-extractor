commit d389688410e8193d1b3c0e77e7fa3d64d11a28fc
Author: JDGrimes <jdg@codesymphony.co>
Date:   Sun Aug 30 11:56:16 2015 -0400

    Simplify and improve ternary handling in XSS sniff

    Most ternary conditions handled by this sniff occur within a set of
    parentheses, and these are handled when an open parenthesis is
    encountered in the token loop. However, the echo “function” is often
    used without parentheses. Because of this, special handling is required
    for ternary conditions that are used in an echo statement without
    parentheses.

    Previously the code for detecting this scenario was unnecessarily
    complex, and incorrectly identified some ternary conditions as not
    occurring in parentheses when they actually did. The result of this was
    twofold: first, it would sometimes flag an expression as needing to be
    escaped when it did not; and second, it could also skip over an
    expression that did need to be escaped without flagging it.

    This is now fixed.

    Fixes #421