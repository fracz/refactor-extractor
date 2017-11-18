commit 72611d1337ce493259edde275f583e2bae5e221c
Author: Nikolay Krasko <nikolay.krasko@jetbrains.com>
Date:   Wed Jul 5 14:55:40 2017 +0300

    Fix extract refactoring for android extensions declarations (KT-11048)

    Allow any target declarations in marking references. Otherwise conflicts
    for references resolved to xml are not considered broken.

    This also fix evaluate for extension fields.

     #KT-11048 Fixed