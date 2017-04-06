commit ec3c4f94c79e23c29abcde6e1d2f6eaf05e0664c
Author: Tobias Bosch <tbosch1009@gmail.com>
Date:   Tue Nov 19 20:42:38 2013 -0800

    refactor($sce): Use $sniffer instead of $document for feature detection.

    Also adds `$sniffer.msieDocumentMode` property.

    Closes #4931
    Closes #5045