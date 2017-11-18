commit 317a269f17e0ebb3a5d210b80860b681ffbdd923
Author: fzaiser <fzaiser@google.com>
Date:   Wed Aug 23 16:40:30 2017 +0200

    Refactor: Parse return statements without an expression properly

    This is an internal refactoring necessary for the Skylark linter.
    It does not change any behavior.

    RELNOTES: None
    PiperOrigin-RevId: 166199367