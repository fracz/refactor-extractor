commit 6d026067f11ab5f6d44aa6e6b7d1ef0bc257ad82
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sat Feb 2 22:13:53 2013 +0100

    Create Descriptors and Serializer

    The goal for this coding session was to have an 80% functional series of
    descriptors and to implement 2 serializer, one for the default php version
    and one for igbinary.

    In addition was the code consuming the parser refactored to use the DIC
    provided by pimple and extract dependencies away from the ParseCommand.