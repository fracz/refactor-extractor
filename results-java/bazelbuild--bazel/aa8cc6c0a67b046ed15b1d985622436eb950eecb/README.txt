commit aa8cc6c0a67b046ed15b1d985622436eb950eecb
Author: laurentlb <laurentlb@google.com>
Date:   Thu Aug 17 15:39:50 2017 +0200

    Remove validate() methods in the AST, use a visitor instead.

    This is a simple refactoring, no change in behavior.

    RELNOTES: None.
    PiperOrigin-RevId: 165572028