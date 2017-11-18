commit d4b8e967977a2c878ae57a8dca819368e479e693
Author: Andre Bandarra <andreban@google.com>
Date:   Wed Oct 28 15:48:08 2015 +0000

    Added XmlDecompressor

    Created a new XmlDecompressor that
    uses LittleEndianInputStream and reuses
    strings.
    It supports handling namespaces and CData.
    Also improved code readbility.