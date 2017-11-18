commit 34991d212e0266d83d4816a9e72e340c41e9b379
Author: Greg Estren <gregce@google.com>
Date:   Wed Jul 27 14:22:41 2016 +0000

    Clean up DependencyResolver's interface for the dynamic config migration and for general readability.

    Major changes:
     - Remove the intermediate Attribute -> LabelAndConfiguration multimap (computed in resolveAttributes). Instead, feed discovered values directly into the final Attribute -> Dependency map via a new RuleResolver interface.
     - Remove all references to LabelAndConfiguration. The configuration is always the owning rule's configuration except for two special cases: late-bound attributes with splits and late-bound attributes with LateBoundDefault.useHostConfiguration. The original interface made this very unclear and required a lot of awkward and sometimes incorrect logic. The new interface only involves configurations for the cases that actually need them.
     - Remove an ugly hack caused by BuildConfiguration.evaluateTransition mixing poorly with LateBoundDefault.useHostConfiguration (https://github.com/bazelbuild/bazel/blo[]e172693c27f3efc95ed163e43a9f0a7a6fb4017/src/main/java/com/google/devtools/build/lib/analysis/DependencyResolver.java#L488).
     - Remove a hack that applies split transitions twice because of BuildConfiguration.evaluateTransition mixing poorly with late-bound split attributes (https://github.com/bazelbuild/bazel/blo[]e172693c27f3efc95ed163e43a9f0a7a6fb4017/src/main/java/com/google/devtools/build/lib/analysis/DependencyResolver.java#L319). This happens to be innocent now but won't be when nested splits are possible.
    - Solidifies the API contract for Attribute.LateBoundDefault.useHostConfiguration.
    - Applies clearer naming and more consistent ordering to method parameters.
    - Better documentation.

    This is all also prep work for dynamic split transitions.

    tl;dr: late-bound attributes are legitimately special. Treat them that way to make the rest of DependencyResolver cleaner and hack-free.

    --
    MOS_MIGRATED_REVID=128582618