commit ad1a77e296fe5f05624109f3af04c5bc3257e9b6
Author: Greg Estren <gregce@google.com>
Date:   Wed Sep 28 14:49:12 2016 +0000

    Eliminates performance overhead of --experimental_dynamic_configs=notrim.

    Before this change, a non-trivial real-world build was measured to have about 20% analysis time overhead. After, that's theoretically down to 2.4%. But that's only the analysis phase, so the impact on full builds is smaller. And the impact on analysis-cached builds is zero. And practical tests show no obvious difference (JProfiler is probably overstating the impact since it excludes known heavyweight methods).

    The improvements, in short:
     - Optimize a sanity check that expects each <Attribute, Label> pair to only have one transition. This alone was over half the original performance penalty.
     - Simplify logic for null configuration deps (of which there are many, e.g.: all source files)
     - Skip a check for required fragments not available in the configuration. This is irrelevant for notrim mode.

    There are still some places we could optimize. Dependency.withNullConfiguration in particular takes measurable time (I think from being constructed all the time and in its hashCode calls). But this doesn't seem pressing given the new numbers.

    --
    MOS_MIGRATED_REVID=134533452