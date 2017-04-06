commit 267016758de3143e80244b266a8b27978b115320
Author: kimchy <kimchy@gmail.com>
Date:   Tue Jul 6 18:21:36 2010 +0300

    improve handling of memory caching with file system, only force compound file when really needed (when an extension that exists within the compound file is part of the memory cached extensions)