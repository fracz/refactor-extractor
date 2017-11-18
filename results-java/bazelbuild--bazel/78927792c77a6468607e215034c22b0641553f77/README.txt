commit 78927792c77a6468607e215034c22b0641553f77
Author: cparsons <cparsons@google.com>
Date:   Wed Oct 11 00:14:19 2017 +0200

    Refactor ConfiguredAttributeMapper to lib/packages from lib/analysis.

    This requires moving the convenience constructor using RuleConfiguredTarget to be owned by RuleConfiguredTarget.

    This refactoring is required by later work to allow SplitTransitionProvider to use configurable attributes. This would require packages/Attribute.java -> analysis/ConfiguredAttributeMapper.java, where in general, the 'analysis' package depends on the 'packages' package. This is the easiest way to prevent a circular dependency.

    RELNOTES: None.
    PiperOrigin-RevId: 171741620