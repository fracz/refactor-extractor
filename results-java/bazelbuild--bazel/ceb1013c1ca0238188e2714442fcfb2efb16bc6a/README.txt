commit ceb1013c1ca0238188e2714442fcfb2efb16bc6a
Author: ccalvarin <ccalvarin@google.com>
Date:   Tue Oct 10 05:29:56 2017 +0200

    Report the structured Bazel command line via the BEP.

    This is part of the effort outlined in https://bazel.build/designs/2017/07/13/improved-command-line-reporting.html. The refactoring of the options parser is not yet complete, so we still do not have complete & correct information about the canonical command line. Where the information is blatantly incorrect, a best approximation was made, with comments and tests documenting the deficiencies.

    Change the names of the initial CommandLine fields in the BEP to be explicitly identified as unstructured.

    RELNOTES: None.
    PiperOrigin-RevId: 171625377