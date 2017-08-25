commit 945554c48a1fa9ca9904ce4eaccfa0c3d53474ec
Author: Mike van Riel <me@mikevanriel.com>
Date:   Fri May 19 18:51:57 2017 +0200

    Sourcecode flag does not function

    During a refactoring the sourcecode flag was broken; with this change the
    source code flag is responsible again for including the source code and
    without it the source code is no longer available.