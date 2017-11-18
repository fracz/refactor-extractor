commit ba637b4d1f1b0c48c62498cc35ac6f5665cf4f27
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Tue Feb 7 17:27:20 2012 -0600

    add getRangeKeySample and refactor key sampling to use more-efficient CFS.keySamples
    patch by Sam Tunnicliffe and jbellis for CASSANDRA-2917