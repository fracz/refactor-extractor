commit 3027073602221df740b81f144e0d74e50d52d210
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Fri Apr 22 18:28:38 2016 +0300

    refactoring: move common ClipboardAnalyzeLister to platform-impl to be able process several thing on the fly

    * reuse new common listener in AnalyzeStacktrace;
    * move get text from clipboard to common util method;