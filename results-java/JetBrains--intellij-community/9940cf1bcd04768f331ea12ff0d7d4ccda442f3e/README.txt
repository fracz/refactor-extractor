commit 9940cf1bcd04768f331ea12ff0d7d4ccda442f3e
Author: Dmitry Batrak <Dmitry.Batrak@jetbrains.com>
Date:   Wed Feb 5 12:41:06 2014 +0400

    IDEA-80056 Column selection mode improvement

    revert PasteHandler - make paste work more reliably in multi-caret state, disabling language-specific paste postprocessors for now