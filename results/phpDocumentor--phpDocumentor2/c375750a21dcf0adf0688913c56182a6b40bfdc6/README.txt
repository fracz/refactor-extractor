commit c375750a21dcf0adf0688913c56182a6b40bfdc6
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sat Jul 7 10:35:37 2012 +0200

    Started refactoring of current EventDispatcher to Symfony2's EventDispatcher.

    Removed the old EventDispatcher and introduced a Proxy EventDispatcher and
    Event class that is capable of relaying and transforming data from
    phpDocumentor.

    The proxy classes also provide some backwards compatibility with the old
    mechanism since the difference between sf1 and sf2's EventDispatcher is
    rather large.

    With the move are Event objects introduced; these are recommended by
    Symfony but also a good idea for data transformations and to make the
    available events more visible.

    What is missing is a way to retrieve the events (some sort of factory)
    and tests.

    This commit is not stable and will introduce bugs; these will be fixed
    in subsequent commits.