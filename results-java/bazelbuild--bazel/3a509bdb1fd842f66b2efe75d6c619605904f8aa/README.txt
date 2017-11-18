commit 3a509bdb1fd842f66b2efe75d6c619605904f8aa
Author: Nathan Harmata <nharmata@google.com>
Date:   Tue Oct 6 01:00:47 2015 +0000

    In FilesystemValueChecker, use 200 threads and don't waste threads on skipped keys. This yields some noticeable improvements for the wall times of null builds with even a small set of files to be checked for changes.

    --
    MOS_MIGRATED_REVID=104717653