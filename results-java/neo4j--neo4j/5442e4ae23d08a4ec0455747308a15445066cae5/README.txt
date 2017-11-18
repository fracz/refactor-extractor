commit 5442e4ae23d08a4ec0455747308a15445066cae5
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Thu Sep 18 10:45:04 2014 +0200

    Fix an issue reading transaction logs over network.

    NetworkReadableLogChannel should not check if there is enough data available before reading.
    Netty will throw an IndexOutOfBoundsException if the network stream doesn't supply enough data
    to match the requested type.

    ReadPastEndException was refactored as a singleton without stack trace as it is a common and
    expected case in the code paths.

    Also removed some logging previously added for debugging this issue.