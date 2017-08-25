commit 82f1f96692ff593df5975407b1e44eb875f35cc3
Author: David Persson <davidpersson@gmx.de>
Date:   Wed Jan 11 18:00:54 2012 +0100

    Improving performance of `String::insert` by adding `is_scalar` check.

    Wrapping check in `String::insert()` in check for scalar type.

    Removing explicit check if object is a closure. This is implied when
    checking for the `__toString` on object. Closures don't have such
    a method.

    Benchmarks show this saves roughly 6% CPU time when using just
    strings as replacement (which is the most common usage scenario).

    Exposing `is_object` outside the enclosing if clause
    makes it eating ~10% of CPU time.

    Removing `is_object` may seem like an option because it is implied
    by `method_exists` but this turns out to be way slower for non objects.

    Adding note about future refactorings.

    Removing todo/note for optimization.