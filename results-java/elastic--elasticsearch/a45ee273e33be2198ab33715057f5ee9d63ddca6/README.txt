commit a45ee273e33be2198ab33715057f5ee9d63ddca6
Author: Alex Ksikes <alex.ksikes@gmail.com>
Date:   Wed Sep 2 21:45:56 2015 +0200

    MLT: builder takes a new Item object like its parser

    Previously the parser could take any Term Vectors request, but this would be
    not the case of the builder which would still use MultiGetRequest.Item. This
    introduces a new Item class which is used by both the builder and parser.

    Beyond that the rest is mostly cleanups such as:

    1) Deprecating the ignoreLike methods, in favor to using unlike.

    2) Deprecating and renaming MoreLikeThisBuilder#addItem to addLikeItem.

    3) Ordering the methods of MoreLikeThisBuilder more logically.

    This change is needed for the upcoming query refactoring of MLT.

    Closes #13372