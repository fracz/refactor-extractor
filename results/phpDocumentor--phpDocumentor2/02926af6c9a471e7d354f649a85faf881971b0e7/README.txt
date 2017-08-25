commit 02926af6c9a471e7d354f649a85faf881971b0e7
Author: Mike van Riel <me@mikevanriel.com>
Date:   Wed May 21 22:50:51 2014 +0200

    #1268: Restore handling of visibility

    During the refactoring of Descriptors I had implemented visibility as a setting on
    the Project Descriptor. Apparently the wiring was never finished or regressed because
    the visibility is not taken into account when provided.

    With this commit I have restored the functionality to filter on specific visibilities.