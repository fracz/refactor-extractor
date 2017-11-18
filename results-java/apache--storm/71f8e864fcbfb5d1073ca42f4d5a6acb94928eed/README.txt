commit 71f8e864fcbfb5d1073ca42f4d5a6acb94928eed
Author: Jeff Fenchel <jfenc91@gmail.com>
Date:   Sat Sep 10 18:32:35 2016 -0700

    STORM-2087: storm-kafka-client improvements for kafka 0.10
     - Added unit tests to storm-kafka-client
     - Fixed bug in kafka-spout-client that resulted in tuples not being  replayed
        when a failure occurred immediately after the last commit
     - Modified the kafka spout to continue processing and not halt upon receiving
        a double ack