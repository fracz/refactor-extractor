commit 077932a740f51baed08a68fd1b11a6c95a535a3c
Author: Chris Beams <chris@gradle.com>
Date:   Fri Oct 16 15:07:41 2015 +0200

    Refactor JvmComponentPlugin

    The following stylistic changes are made here in preparation for
    substantive changes to this class in subsequent commits:

     - Format Javadoc for readability

     - Use public visibility consistently on rule-annotated methods

     - Refactor #createBinaries and its related private methods for
       readability and elimination of unused parameters