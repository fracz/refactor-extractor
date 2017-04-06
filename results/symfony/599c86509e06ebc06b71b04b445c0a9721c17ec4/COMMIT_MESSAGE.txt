commit 599c86509e06ebc06b71b04b445c0a9721c17ec4
Merge: 08ec911 1b2ef74
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Sep 8 22:22:53 2013 +0200

    merged branch fabpot/request-stack (PR #8904)

    This PR was merged into the master branch.

    Discussion
    ----------

    Synchronized Service alternative, backwards compatible

    This is a rebased version #7707.

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #7707
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#2956

    Todo/Questions

     - [x] do we deprecate the synchronize feature (and removed it in 3.0)?
     - [x] deal with BC for listeners
     - [x] rename RequestContext as we already have a class with the same name in the Routing component?
     - [x] move RequestStack and RequestContext to HttpFoundation?
     - [x] update documentation

    Prototype for introducing a ``RequestContext`` in HttpKernel.

    This PR keeps the synchronized services feature, however introduces a ``RequestContext`` object additionally, that allows to avoid using synchronized service when injecting ``request_context`` instead of ``request`` into a service. The FrameworkBundle is modified such that it does not depend on synchronized services anymore. Users however can still depend on ``request``, activating the synchronized services feature.

    Features:

    * Introduced ``REQUEST_FINSHED`` (name is up for grabs) event to handle global state and environment cleanup that should not be done in ``RESPONSE``. Called in both exception or success case correctly
    * Changed listeners that were synchronized before to using ``onKernelRequestFinished`` and ``RequestContext`` to reset to the parent request (RouterListener + LocaleListener).
    * Changed ``FragmentHandler`` to use the RequestContext. Added some more tests for this class.

    * ``RequestStack`` is injected into the ``HttpKernel`` to handle the request finished event and push/pop the stack with requests.

    Todos:
    * RequestContext name clashes with Routing components RequestContext. Keep or make unique?
    * Name for Kernel Request Finished Event could be improved.

    Commits
    -------

    1b2ef74 [Security] made sure that the exception listener is always removed from the event dispatcher at the end of the request
    b1a062d moved RequestStack to HttpFoundation and removed RequestContext
    93e60ea [HttpKernel] modified listeners to be BC with Symfony <2.4
    018b719 [HttpKernel] tweaked the code
    f9b10ba [HttpKernel] renamed the kernel finished event
    a58a8a6 [HttpKernel] changed request_stack to a private service
    c55f1ea added a RequestStack class