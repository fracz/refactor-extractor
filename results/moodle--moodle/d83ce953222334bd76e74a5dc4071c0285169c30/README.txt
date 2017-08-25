commit d83ce953222334bd76e74a5dc4071c0285169c30
Author: David Mudrak <david@moodle.com>
Date:   Wed May 30 00:58:50 2012 +0200

    MDL-33330 improved {files_reference} records creation

    This patch unifies the way how records in {files_reference} get created.
    Previously, each reference file (i.e. a file with referencefileid set)
    created its own record in {files_reference}. This patch makes sure that
    existing record is reused if possible.

    Bye bye 1:1 relationships, you suck!