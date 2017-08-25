commit f586981ffb301fd52ed068a9057589273c9ae8e3
Author: Mike van Riel <me@mikevanriel.com>
Date:   Fri May 16 08:55:31 2014 +0200

    #1248: External Router tests broken

    In this commit we fix the broken tests for phpDocumentor after the refactoring
    of the configuration component. In order to properly run these tests we needed
    to adjust the instantiation of the configuration to instantiate the sub-items
    as well.