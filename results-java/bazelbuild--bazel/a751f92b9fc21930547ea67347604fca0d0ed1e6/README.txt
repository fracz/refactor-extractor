commit a751f92b9fc21930547ea67347604fca0d0ed1e6
Author: Michael Staib <mstaib@google.com>
Date:   Tue Feb 14 15:50:04 2017 +0000

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
    Skylark will continue treating it as a dictionary from String to Label
    in its rule context, however, to avoid visible behavior changes.

    --
    PiperOrigin-RevId: 147471542
    MOS_MIGRATED_REVID=147471542