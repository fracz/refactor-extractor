commit 1527356b1e2cd54f21bbe4da4f6783f11d0415f8
Author: Nicholas K. Dionysopoulos <nicholas@akeebabackup.com>
Date:   Wed Feb 25 17:18:15 2015 +0200

    Fix #6173

    JFile::upload was missing the fourth argument, resulting in file content scanning for ZIP file per the default options. Since installation package ZIP files may contain uncompressed .php files OR otherwise the string literals `<?` and `<?php` the upload was rejected. However, installation packages are *supposed* to contain executable code, therefore we should not pass them through the safe file (UploadShield) code.

     Moreover, this patch adds an optional fifth parameter to JFile::upload to allow developers to pass custom options to the safe file scanning (UploadShield) code, as was the original intention of the UploadShield feature when it was committed two years ago but lost during the refactoring for inclusion in Joomla! 3.4.

     Finally, the docblock for isSafeFile was reformatted for improved clarity of the parameters which can be passed to UploadShield.