commit 57daac0d426e75914dccc122d322a74eb72685ba
Author: gregce <gregce@google.com>
Date:   Wed Apr 12 17:42:04 2017 +0000

    Add select() support for "query --output=build".

    Before this change, attributes with select() were completely skipped.

    This doesn't attempt to merge "trivial" selects, e.g. even though:

      attr = [":foo"] + select({"//conditions:default": [":bar"]})

    always resolves to:

      attr = [":foo", ":bar"]

    this change still produces:

      attr = [":foo"] + [":bar"]

    We could merge these in a future change, if desired. But it's not
    even clear that's desired. There's conceptual value in keeping the
    lists separate since that's how they were originally written. That
    gives users a cue to maybe refactor their rules.

    RELNOTES[NEW]: "query --output=build" now includes select()s

    PiperOrigin-RevId: 152956939