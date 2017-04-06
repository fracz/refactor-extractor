commit 4009280802a6f6589ed2a60a5a467294117f3736
Merge: db9a008 32ec288
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Dec 17 09:24:59 2016 +0100

    feature #20962 Request exceptions (thewilkybarkid, fabpot)

    This PR was merged into the 3.3-dev branch.

    Discussion
    ----------

    Request exceptions

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | yes
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #20389, #20615, #20662
    | License       | MIT
    | Doc PR        | n/a

    Replaces #20389 and #20662. The idea is to generically manage 400 responses when an exception implements `RequestExceptionInterface`.

    The "weird" caches on the request for the host and the clients IPs allows to correctly manage exceptions in an exception listener/controller (as we are duplicating the request there, but we don't want to throw an exception there).

    Commits
    -------

    32ec288 [HttpFoundation] refactored Request exceptions
    d876809 Return a 400 response for suspicious operations