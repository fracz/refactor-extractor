commit 7414b873105a2e0435267d5b863caa7df0d57e38
Author: ccalvarin <ccalvarin@google.com>
Date:   Tue Oct 24 01:05:18 2017 +0200

    Improve --config expansion logging under --announce_rc

    To make the source of options more clear before migration, improve the output provided with --announce_rc. This means separating the log messages we have for unconditional rc-options and --config options. The unconditional log statement has not changed. When expanding --config options, log the following in the order that the options are parsed:
    INFO: Found applicable config definition <command>:<configName> in file <rcfile>: <the options config expands to>

    RELNOTES: Improve --config logging when --announce_rc is present.
    PiperOrigin-RevId: 173185451