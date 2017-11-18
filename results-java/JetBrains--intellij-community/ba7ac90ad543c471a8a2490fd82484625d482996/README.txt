commit ba7ac90ad543c471a8a2490fd82484625d482996
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Thu Jul 19 14:15:21 2012 +0400

    [diff/merge] LineBlocks internal refactoring

    Don't use provider: all the data is ready at init;
    Don't work with 3 arrays, use a list of custom Diff structure instead.