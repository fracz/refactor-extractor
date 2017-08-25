commit 9ee15a1589faa16ea0cf674af2e5ca678d557066
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sat Jan 18 11:49:48 2014 +0100

    Extract Property and Constants from XML Writer

    In an effort to refactor the XML Writer to provide better tested and
    SOLID code with which bugs can easier be tracked and resolved we
    have extracted the property and constant handling into their own
    converter classes and added unit tests to verify the behaviour of
    those converters.