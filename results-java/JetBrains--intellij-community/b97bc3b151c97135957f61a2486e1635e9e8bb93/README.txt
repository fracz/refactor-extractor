commit b97bc3b151c97135957f61a2486e1635e9e8bb93
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Tue Feb 28 21:30:30 2012 +0400

    Git refactoring: static methods -> app service

    Make Git an application service interface.
    Put the implementation to GitImpl.
    This will make it more flexible and overridable in tests.