commit 22f261d3041f6ee5ffd1976d5f5b46f8cc828ea0
Author: schmitt <schmitt@google.com>
Date:   Tue May 9 11:30:32 2017 -0400

    Use BazelJavaSemantics for native Exoblaze and GoogleJavaSemantics for Exoblaze compatibility mode.

    The functional change in this CL is that java_mutable_proto library now uses the "correct" java semantics depending on which blaze it is installed in. This required some refactoring of the ExoblazeRulesModule and ExoblazeRuleClassProvider to make them able to have parameters (like java semantics).

    To simplify module installation this change also extracts strategy management from being in "rules" modules to their own modules. Now modules don't need to inherit from each other anymore but can be installed alongside each other.

    It seems to be impossible to test the compatibility mode in TAP: java_mutable_proto library requires both Linux binaries (Blaze itself) and macOS binaries (ijar) which cannot both be inputs to the test today. So instead I manually tested this change in both Exoblaze native and compatibility mode.

    RELNOTES: None.
    PiperOrigin-RevId: 155507096