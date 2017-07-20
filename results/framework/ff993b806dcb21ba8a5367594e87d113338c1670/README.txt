commit ff993b806dcb21ba8a5367594e87d113338c1670
Author: Taylor Otwell <taylor@laravel.com>
Date:   Thu Dec 22 20:51:44 2016 -0600

    Refactor container. Remove old features.

    This is a moderate refactoring of the container to clean up code and
    remove some legacy features that haven’t been documented in several
    years. The make() method no longer accepts a second argument of
    parameters - needing this feature indicates a code smell and you can
    always construct the object in another way. It simply complicated the
    container and code and made it hard to read.

    Secondly, the share() method has been removed. This method was an old
    method to provide backwards compatibility with Pimple. It has not been
    documented in a few years and the more popular “singleton()” method is
    easier to use.

    Thirdly, the Normalize() method has been removed. This is the most
    called method in all of Laravel and it is called almost 1,000 times on
    every request because of the heavy usage of the container. On a fresh
    opcache enabled run of Laravel it is actually uses about 8-9% of the
    entire request processing time. It’s whole purpose is to allow you to
    bind objects into the container with a leading slash; however,
    Foo::class never returns a leading slash and if you pass a string we
    can simply document that a leading slash should not be provided.