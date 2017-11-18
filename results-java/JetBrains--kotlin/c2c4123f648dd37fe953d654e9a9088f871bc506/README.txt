commit c2c4123f648dd37fe953d654e9a9088f871bc506
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Fri Nov 15 21:57:42 2013 +0400

    Don't store object descriptors separately in lazy member scope

    Object descriptors are refactored to be the usual classes, so the scope can't
    contain any 'objects'