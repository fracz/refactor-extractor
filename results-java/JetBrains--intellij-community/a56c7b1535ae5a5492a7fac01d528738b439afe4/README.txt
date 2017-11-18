commit a56c7b1535ae5a5492a7fac01d528738b439afe4
Author: Alexey Kudravtsev <cdr@intellij.com>
Date:   Wed Sep 4 14:10:01 2013 +0400

    editor api modules refactoring:
    - editor-ui-api module containing public API editor interfaces
    - editor-ui-impl module containing editor implementation interfaces
    - analysis* modules made dependant on editor-ui* modules
    - highlighting classes moved to *analysis* modules
    - majority of highlighting quickfixes moved to QuickFixFactory
    - injections in GeneralHighlightingPass splitted away to InjectedGeneralHighlightingPass