commit 077e8cd9d4a835a5972e4b0fbc945208852381c8
Author: Aaron Piotrowski <aaron@trowski.com>
Date:   Tue Mar 3 19:27:54 2015 -0600

    Reorganized stream tests.

    Tests were reorganized to move generalized tests to test/Stream and leave tests specific to stream sockets in test/Socket.

    Any text-based stream should pass test/Stream/ReadableStreamTestTrait and test/Stream/WritableStreamTestTrait.