commit 0e2d708efcf6e2f694dda44bb85355c2e6a5915f
Author: nicolasconnault <nicolasconnault>
Date:   Fri Aug 31 05:55:30 2007 +0000

    MDL-11034 Implemented the global enablepublishing and refactored the handling of userkey for each export plugin. Also added a dump.php file in each plugin directory, although this could easily be refactored into 1 file in the parent folder, pointing to each plugin's export.php file for output differences.