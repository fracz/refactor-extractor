commit 57a8e6674e5bd5abc3a54a7771f09322f19413c7
Author: Michael Staib <mstaib@google.com>
Date:   Mon Feb 13 21:25:25 2017 +0000

    Rollback of commit cdbad585187dfe7bbb4d69ad68a1baf852beb691.

    *** Reason for rollback ***

    Breaks Skylark aspects running over rules with LABEL_DICT_UNARY attributes.

    *** Original change description ***

    Refactoring: Types report what class of labels they contain.

    Currently label-type attributes are detected in many places across the
    codebase by simply reference-comparing against each of the label types.
    This CL aims to generalize most of these cases, moving the encoding of
    this logic into a single place (Type/BuildType itself). Not all of these
    cases can be made general without further refactoring, and some perhaps
    shouldn't be - serialization and Skylark rule context, for example, need
    to do...

    --
    PiperOrigin-RevId: 147385072
    MOS_MIGRATED_REVID=147385072