commit cdbad585187dfe7bbb4d69ad68a1baf852beb691
Author: Michael Staib <mstaib@google.com>
Date:   Fri Feb 10 19:08:13 2017 +0000

    Refactoring: Types report what class of labels they contain.

    Currently label-type attributes are detected in many places across the
    codebase by simply reference-comparing against each of the label types.
    This CL aims to generalize most of these cases, moving the encoding of
    this logic into a single place (Type/BuildType itself). Not all of these
    cases can be made general without further refactoring, and some perhaps
    shouldn't be - serialization and Skylark rule context, for example, need
    to do exotic things based on the type. But most sites can avoid having to
    enumerate all the types they work with explicitly.

    This causes LABEL_DICT_UNARY to start being treated like the other label
    types, which means that CcToolchainSuiteRule and JavaRuntimeSuiteRule
    need to include a set of allowed file types (none, in their case).

    --
    PiperOrigin-RevId: 147175424
    MOS_MIGRATED_REVID=147175424