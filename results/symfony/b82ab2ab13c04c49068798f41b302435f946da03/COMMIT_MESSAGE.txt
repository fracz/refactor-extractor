commit b82ab2ab13c04c49068798f41b302435f946da03
Merge: 05b1755 ab3e231
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Nov 30 10:39:57 2013 +0100

    minor #9661 Removed observer pattern, in favour of mediator (fabpot)

    This PR was merged into the 2.2 branch.

    Discussion
    ----------

    Removed observer pattern, in favour of mediator

    | Q             | A
    | ------------- | ---
    | Doc fix?      | yes
    | New docs?     | no
    | Applies to    | *
    | Fixed tickets | #9344

    I don't think this is the best tool to implement the observer pattern.

    I just have copied the sentence from:
    https://github.com/symfony/symfony-docs/edit/2.3/components/event_dispatcher/introduction.rst

    but maybe could be improved with something like:

    "Symfony Event Dispatcher is a tool that allows to make your projects truly extensible, facilitating the implementation of Mediator pattern and Publish subscribe pattern"

    [edit-1]
    I think this could be modified also here:
    https://github.com/symfony/symfony-marketing/edit/master/views/en/get_started/components.html.twig

    Commits
    -------

    ab3e231 [EventDispatcher] tweaked README
    b9ad62e removed observer pattern, in favour of mediator