commit e850ceb5f0671583e9a2b8166a7bd3779ed17928
Author: NathanSweet <nathan.sweet@gmail.com>
Date:   Sat Sep 13 18:20:30 2014 +0200

    SelectBox refactoring.

    * Now fires change events correctly.
    * Moved select box list functionality to SelectBoxList, code clean up.
    * Better way of hiding select box list, list being open doesn't stop input events elsewhere.
    * List reverts to selected item on mouse exit.