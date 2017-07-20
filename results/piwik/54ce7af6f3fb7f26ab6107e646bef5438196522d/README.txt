commit 54ce7af6f3fb7f26ab6107e646bef5438196522d
Author: Thomas Steur <thomas.steur@googlemail.com>
Date:   Thu Dec 4 20:21:39 2014 +0100

    refs #5144 better fix for array to string to conversion.

    Noticed this hack while I worked on the tracker refactoring. Dispatch
    should always return a string. Fixing the problem where it actually occurs.
    An even better fix would be not to support serialize=0 for format PHP as
    it is meant only for internal requests. If someone wants to access data
    serialize=1 should be set.