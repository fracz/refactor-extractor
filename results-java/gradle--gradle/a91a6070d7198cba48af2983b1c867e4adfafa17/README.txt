commit a91a6070d7198cba48af2983b1c867e4adfafa17
Author: Stefan Wolf <wolf@gradle.com>
Date:   Thu Jul 7 23:45:06 2016 +0200

    Refactor InetAddressFactory

    The main reason for this refactoring was to make
    InetAddressFactory easier to understand. I also extracted
    the responsibility of obtaining the InetAddresses from
    the NetworkInterfaces in a different class. The only
    responsibility of InetAddressFactory is now to provide
    enough data for two processes to communicate via
    IP addresses. As a side effect InetAddressFactory is now testable.

    The allow remote flag is still useful, therefore it has not been removed.
    Setting it allows connections from a remote machine.

    #644
    +review REVIEW-6069