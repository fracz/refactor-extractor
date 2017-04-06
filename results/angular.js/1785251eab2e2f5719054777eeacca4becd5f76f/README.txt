commit 1785251eab2e2f5719054777eeacca4becd5f76f
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Sat Oct 18 01:07:49 2014 +0200

    refactor($sce): don't depend on $document

    The $sce dependency on $document was added in 64241a5 because it was thought
    it's not possible to easiy use the msie variable in this module. This was
    changed in 45252c3, though so it's no longer needed to depend on $document.

    Closes #9671