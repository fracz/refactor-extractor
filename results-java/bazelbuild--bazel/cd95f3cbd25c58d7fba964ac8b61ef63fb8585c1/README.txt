commit cd95f3cbd25c58d7fba964ac8b61ef63fb8585c1
Author: gregce <gregce@google.com>
Date:   Mon Jun 5 15:33:48 2017 -0400

    Make compatible_with = ["all", "foo"] the same as compatible_with = ["all"].

    Assuming "all" fulfills "foo", these should be exactly the same (and
    maybe we should trigger a redundant listing error). In practice, it's
    possible to make the first case succeed while the second fails because of
    environment refining and lack of static constraint checking for selects.

    See changes for details.

    Also refactor ConstraintSemantics.checkConstraints to divide and conquer more clearly.

    PiperOrigin-RevId: 158047217