commit 3a95f353704dc2f7061e2c0786c2459ac1db0fd1
Author: Nathan Harmata <nharmata@google.com>
Date:   Fri Feb 5 00:11:55 2016 +0000

    Use a clever hybrid approach for evaluating globs during package loading: first try to get a skyframe cache-hit; otherwise, fall back to legacy globbing. This gives us the best of both worlds: no extra skyframe restarts on glob-dep-misses, and much better incremental performance in the common case that a package's globs haven't changed. See the class-comment for PackageFunction.SkyframeHybridGlobber for a detailed description and explanation.

    This CL has no impact on semantics and is a strict performance win.

    Bazel users: Here's an example benchmark (does an incremental loading phase of a target T but forces all packages to be reloaded):

    nharmata@nharmata:~/bazel$ N=10; B="output/bazel"; T=//src/main/java/com/google/devtools/build/lib:bazel/BazelServer_deploy.jar; CMD="build --noanalyze $T"; $B clean &> /dev/null; P=base_workspace/tools/build_rules/prelude_bazel; rm $P; touch base_workspace/tools/build_rules/BUILD; $B $CMD &> /dev/null; time for i in $(seq 1 $N); do echo "#hi" >> $P; $B $CMD &> /dev/null; done

    For a very large internal Google target, this CL improves the benchmark performance by ~6%. A more targeted benchmark would be for loading a single package that has lots of expensive globs. For example, the time to incrementally load a single pathological Google-internal package was reduced by ~36%.

    Alternatives considered: Introduce skyframe native globbing, gated by flags for both globbing during preprocessing and globbing during regular BUILD file evaluation. The approach in this CL is superior performance-wise.

    --
    MOS_MIGRATED_REVID=113899687