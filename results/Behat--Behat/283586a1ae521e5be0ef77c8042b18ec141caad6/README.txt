commit 283586a1ae521e5be0ef77c8042b18ec141caad6
Author: everzet <ever.zet@gmail.com>
Date:   Sat Sep 24 23:53:44 2016 +0100

    Further improve context generation logic

    Currently, when not using `SnippetAcceptingContext` interface (as
    it is deprecated), people are forced to use the `--snippets-for`
    flag in CLI. However, it is now obvious that for newcomers it is
    not transparent that `--snippets-for` has an interactive mode
    when called without argument. That causes unnecessary confusion.

    This commit refactors slightly both pattern and context class
    identifiers to make both more flexible. As a result, context
    identifiers as of this commit operate in the following way:

    1. If suite has any context implementing `SnippetAcceptingContext`,
       that class will be used for snippet generation.
    2. If above is not true, but user explicitly provided a context
       class via `--snippets-for`, the provided class will be used
       instead.
    3. If none of the above is  true, but the current CLI environment
       supports interactive mode - interactive mode will kick in
       automatically.
    4. If none of the above is true, the help message asking user to
       use `--snippets-for` will be provided.