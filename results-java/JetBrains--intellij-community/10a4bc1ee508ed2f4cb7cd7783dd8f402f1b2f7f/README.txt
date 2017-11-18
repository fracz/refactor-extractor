commit 10a4bc1ee508ed2f4cb7cd7783dd8f402f1b2f7f
Author: Oleg Sukhodolsky <oleg.sukhodolsky@jetbrains.com>
Date:   Thu Oct 11 17:14:33 2012 +0400

    RUBY-10872: (refactoring) let's use ProcessOutput instead of Output.
    Remote interpreters use only ProcessOutput and we need the same code to work with both remote and local interpreters.