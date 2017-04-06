commit e5275590db1e7c2d7cf7bb0c44e92a6f9e7518e7
Author: Lucas Mirelmann <lgalfaso@gmail.com>
Date:   Thu Nov 5 21:56:08 2015 +0100

    refactor($interval): do not use `notify` to trigger the callback

    Do not use `$q.notify` to trigger the callback. This allows `$q` to be replaced
    with another Promise/A+ compilant library

    Closes: #13261