commit 5b6302405b2039f74e2259fa1829cdf7eee7f6ac
Author: Marcel Hlopko <hlopko@google.com>
Date:   Thu Dec 22 08:48:05 2016 +0000

    Move hardcoded toolchain linker flags to CROSSTOOL

    This cl introduces another build variable: toolchain_flags and updates all the
    affected crosstools (those which started using action_configs, because the
    defaults from CppLinkActionConfigs are not applied then). This build variable
    is a requirement for follow-up refactoring exposing param_files build variable.
    With toolchain_flags and param_files we will have full control over flags
    placement on the link command line.

    --
    PiperOrigin-RevId: 142741060
    MOS_MIGRATED_REVID=142741060