commit 31c84951dc4fb964f72dfd2519c3b259ac2285eb
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Wed Nov 20 23:18:44 2013 +0400

    Fix objects appearing in type position in completion

    After the object refactoring they're present as classes in scopes, not as
    object descriptors