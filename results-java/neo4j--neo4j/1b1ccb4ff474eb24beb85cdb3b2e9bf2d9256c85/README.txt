commit 1b1ccb4ff474eb24beb85cdb3b2e9bf2d9256c85
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Tue Jul 19 11:19:15 2016 +0200

    AdminTool internals refactorings

    This makes it slightly easier to test command providers, and also aligns the visibilities.
    For instance, methods that returned `Result` objects were publicly visible, while the `Result` interface was private.