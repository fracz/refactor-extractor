commit a14e0af554bcdafdb8f8f86e1aabc617775dd9d7
Author: Michal Čihař <michal@cihar.com>
Date:   Wed Feb 17 11:53:17 2016 +0100

    Simplify and improve message about listing bookmarks

    The string concatenation makes it really poor for translating, usually
    leading to sentences which do not make sense.

    Signed-off-by: Michal Čihař <michal@cihar.com>