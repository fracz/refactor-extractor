commit d5f0ecb407f5708841e98e55d01d651a316ae7c2
Author: nharmata <nharmata@google.com>
Date:   Mon Nov 6 21:39:43 2017 +0100

    Some minor quality of life improvements related to the fact that the default value of --max_idle_secs is 15s when the TEST_TMPDIR environment variable is set.

    (i) Add a log line to blaze.INFO when the server shuts itself down due to idleness.
    (ii) Mention the --max_idle_secs default in the existing stderr spam when TEST_TMPDIR is set.

    RELNOTES: None
    PiperOrigin-RevId: 174745511