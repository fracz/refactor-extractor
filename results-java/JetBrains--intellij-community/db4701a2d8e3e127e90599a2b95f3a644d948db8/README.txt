commit db4701a2d8e3e127e90599a2b95f3a644d948db8
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Apr 1 16:49:30 2015 +0300

    Slightly refactor PyExtractMethodHandler

    * Renamed PyPsiUtil#getStatement to PyPsiUtil#getParentRightBefore as
    this name more clearly describes method's functionality.
    * Return Couple<PsiElement> instead of awkward two-items array or
    PsiElements.