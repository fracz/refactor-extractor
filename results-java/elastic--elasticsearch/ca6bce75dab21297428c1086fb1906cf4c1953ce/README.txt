commit ca6bce75dab21297428c1086fb1906cf4c1953ce
Author: Jason Tedor <jason@tedor.me>
Date:   Wed Sep 13 21:30:27 2017 -0400

    Refactor bootstrap check results and error messages

    This commit refactors the bootstrap checks into a single result object
    that encapsulates whether or not the check passed, and a failure message
    if the check failed. This simpifies the checks, and enables the messages
    to more easily be based on the state used to discern whether or not the
    check passed.

    Relates #26637