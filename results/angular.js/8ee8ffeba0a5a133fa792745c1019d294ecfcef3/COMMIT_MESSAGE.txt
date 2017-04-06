commit 8ee8ffeba0a5a133fa792745c1019d294ecfcef3
Author: Lucas Galfaso <lgalfaso@gmail.com>
Date:   Sat Sep 6 01:40:26 2014 +0200

    fix(linky): encode double quotes when serializing email addresses

    Email addresses can (under certain restrictions) include double quote
    characters. See http://tools.ietf.org/html/rfc3696#section-3.

    For example, `"Jo Bloggs"@abc.com` is a valid email address.

    When serializing emails to the `href` attribute of an anchor element,
    we must HTML encode these double quote characters. See
    http://www.w3.org/TR/html-markup/syntax.html#syntax-attr-double-quoted

    This commit does not attempt to improve the functionality (i.e. regex)
    that attempts to identify email addresses in a general string.

    Closes #8945
    Closes #8964
    Closes #5946
    Closes #10090
    Closes #9256