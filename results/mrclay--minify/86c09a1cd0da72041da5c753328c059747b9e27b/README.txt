commit 86c09a1cd0da72041da5c753328c059747b9e27b
Author: Steve Clay <steve@mrclay.org>
Date:   Wed Sep 3 19:42:41 2008 +0000

    builder app improvements
    Minify.php : + setDocRoot() for IIS (min only checks if on Windows)
    Controller/Page.php : + 'file' option for simpler usage