commit 5a77f426e0896031973ce1dc965f05e014ee9a24
Author: fzaiser <fzaiser@google.com>
Date:   Mon Sep 11 19:38:38 2017 +0200

    Skylint: improve the naming conventions checker

    Changes in behavior:
     * Local variables are also allowed to be UPPER_SNAKE_CASE.
       Upper case means that they're constants but this is not checked yet.
     * Providers are required to be UpperCamelCase. A variable FooBar
       is considered a provider if it appears in an assignment of the form
       "FooBar = provider(...)"
     * Shadowing of builtins (e.g. "True = False", "def fail()") is
       not allowed
     * The single-letter variable names 'O', 'l', 'I' are disallowed
       since they're easy to confuse
     * Multi-underscore names ('__', '___', etc.) are disallowed
     * Single-underscore names may only be written to, as in
         a, _ = tuple
       They may not be read, as in "f(_)".

    In the process, I also moved some code from UsageChecker to
    AstVisitorWithNameResolution to prevent duplication in
    NamingConventionsChecker.

    RELNOTES: none
    PiperOrigin-RevId: 168250396