commit 4b3f9dbf813d7a5def8aac844a3a1023f04fd61a
Author: ajmichael <ajmichael@google.com>
Date:   Fri May 5 20:32:18 2017 +0200

    Create new android_instrumentation rule.

    This rule is responsible for building the target and instrumentation APKs used by an Android instrumentation test. If they are provided as APKs (e.g. from an android_binary or a genrule) they will be used as is. If they are provided as libraries, APKs will be created. This CL does not actually implement building target and instrumentation APKs from libraries, that will come in a follow-up CL as it will require some heavy refactoring of AndroidBinary.java.

    Follow-up CLs will add features such as repackaging the APKs to remove duplicate classes, reproguarding the target APK with the test code, validating that the target and instrumentation APKs were signed with the same debug key and verifying that instrumentation stanza appears in the instrumentation APKs manifest.

    Note that this CL does _not_ install the rule in the BazelRuleClassProvider, so
    this CL does not make it usable by anyone. Once the other android testing rules are ready, I will install them all.

    One small step towards https://github.com/bazelbuild/bazel/issues/903.

    RELNOTES: None
    PiperOrigin-RevId: 155220900