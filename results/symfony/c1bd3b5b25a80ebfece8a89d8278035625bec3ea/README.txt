commit c1bd3b5b25a80ebfece8a89d8278035625bec3ea
Merge: e94346e 4f9a55a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 25 21:03:44 2013 +0100

    merged branch fabpot/console-dispatcher (PR #7466)

    This PR was merged into the master branch.

    Discussion
    ----------

    Console dispatcher

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #3889, #6124
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#2352

    refs #1884, #1929

    This is an alternative implementation for adding events to console applications.

    This implementation has the following features:

    * Available for anyone using the Console component and it is not tied to
      FrameworkBundle (this is important as one thing we are trying to solve is
      email sending from a command, and frameworks like Silex using the Console
      component needs a solution too);

    * Non-intrusive as the current code has not been changed (except for renaming
      an internal variable that was wrongly named -- so that's not strictly needed
      for this PR)

    * The new DispatchableApplication class also works without a dispatcher,
      falling back to the regular behavior. That makes easy to create applications
      that can benefit from a dispatcher when available, but can still work
      otherwise.

    * Besides the *before* and *after* events, there is also an *exception* event
      that is dispatched whenever an exception is thrown.

    * Each event is quite powerful and can manipulate the input, the output, but
      also the command to be executed.

    Commits
    -------

    4f9a55a refactored the implementation of how a console application can handle events
    4edf29d added helperSet to console event objects
    f224102 Added events for CLI commands