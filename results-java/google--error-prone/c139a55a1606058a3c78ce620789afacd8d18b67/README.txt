commit c139a55a1606058a3c78ce620789afacd8d18b67
Author: Liam Miller-Cushon <cushon@google.com>
Date:   Fri Jul 18 13:51:25 2014 -0700

    Change bug marker format for test files.

    This change improves the bug marker format we use for test files.  Previously
    the bug markers looked like:
    //BUG: Suggestion includes "foo.bar()"

    With this change, they look like:
    // BUG: Diagnostic contains: foo.bar()

    This change also allows you to check that multiple substrings all appear in the
    diagnostic:
    // BUG: Diagnostic contains: foo.bar()
    // bar.baz()
    // baz.foo()
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=69778023