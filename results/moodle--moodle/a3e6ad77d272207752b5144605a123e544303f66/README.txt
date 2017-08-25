commit a3e6ad77d272207752b5144605a123e544303f66
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Wed Jun 27 16:17:36 2012 +0100

    MDL-34066 adaptive behaviour: refactor logic out of renderer.

    These changes move the logic to the behaviour class, which is how things
    should be.

    It also makes it easier to re-use the code that displays the messages
    like "Marks for this submission: 1.00/1.00. Accounting for previous
    tries, this gives 0.33/1.00." in other places.

    To try to make it clearer what is going on, I introduced a new class
    qbehaviour_adaptive_mark_details to hold and document the data that the
    behaviour needs to return, and the renderer can base its display on.

    As far as my testing can tell (and there are new unit tests), this
    commit does not change the existing behaviour.

    This commit also replaces all the string concatenation that is going on,
    which should help translators. At the moment, a few of the old strings
    are still in the language file, and are used in the unit tests to verify
    that the behaviour has not changed.

    Thanks to Oleg Sychev for making a helpful suggestion about the API.