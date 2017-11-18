commit b26e97abc87d0f0b713e980203fb85bc5b40b7fd
Author: Vladyslav Zhukovskii <vzhukovskii@codenvy.com>
Date:   Wed Feb 24 17:24:44 2016 +0200

    Fix equality bug in paste action

    Also was performed little refactoring to make code more readable.
    Also was added utility class which works with path's. This is client adopted utility class which is used on the server side jdt implementation. Can be useful for third-paty extensions.

    Relevant issue: CHE-572

    Signed-off-by: Vladyslav Zhukovskii <vzhukovskii@codenvy.com>