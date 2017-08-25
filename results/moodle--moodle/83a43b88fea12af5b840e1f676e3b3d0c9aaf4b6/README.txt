commit 83a43b88fea12af5b840e1f676e3b3d0c9aaf4b6
Author: Ruslan Kabalin <r.kabalin@lancaster.ac.uk>
Date:   Fri Mar 4 14:32:09 2016 +0000

    MDL-50888 antivirus_clamav: Unit tests refactoring.

    Due to configurable nature of scanning method selection, unit test needs to
    be updated to always point to commadline method. There is no need to write
    separate tests for socket scanning method, as the scanning method needs to
    be mocked anyway (i.e. will be identical to commandline scanning from test
    perspective).