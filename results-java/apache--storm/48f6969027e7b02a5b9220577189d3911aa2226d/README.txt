commit 48f6969027e7b02a5b9220577189d3911aa2226d
Author: Stig Rohde Døssing <srdo@apache.org>
Date:   Mon Jul 31 20:26:55 2017 +0200

    STORM-2648/STORM-2357: Add storm-kafka-client support for at-most-once processing and a toggle for whether messages should be emitted with a message id when not using at-least-once

    * Minor refactor of emit statements
    * Add tests for at-most-once and any-times mode, deduplicate some test code in other tests
    * Fix rebase conflicts and fix leaking state through unit test retry service
    * Update storm-kafka-client doc