commit b8ed0c65f027e4949ecb302d7956246c2693b02d
Author: xtof <xtof@google.com>
Date:   Thu Mar 24 20:53:18 2016 -0700

    RefasterJS: Improve type matching of templates.

    Instead of using loose matching, which can result in post-refactoring code that
    does not type-check, use a strict type matching that matches only the exact
    type specified in the template, or proper subtypes thereof.

    To allow refactoring of code that does not quite match types as needed in the
    after-template, as well as un-typed code, this CL changes RefasterJS to attempt
    template matches strictly in the order in which the before-templates appear in
    the template file.

    This allows multiple before-templates of a given pattern to be specified with
    successively more loose type matching, and appropriate after-templates.

    See examples/navigational_xss_sinks.js for an extensive example.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=118178934