commit cef7c9265699e1d74c71ab9839eee515e0074df7
Author: Stig Rohde Døssing <srdo@apache.org>
Date:   Sat Aug 12 16:56:45 2017 +0200

    STORM-2666: Fix storm-kafka-client spout sometimes emitting messages that were already committed. Expand tests, add some runtime validation, minor refactoring to increase code readability. Ensure OffsetManager commits as many offsets as possible when an offset void (deleted offsets) occurs, rather than just up to the gap.