commit 173a838f8c40ec8a3e10e656f8e8497e9302d8a7
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Mar 9 15:01:25 2016 +0300

    Minor, improve some usages of function type utilities

    Re change in FunctionsHighlightingVisitor:
    KBI#isFunctionOrExtensionFunctionType already takes care of supertypes, no need
    to do additional loop