commit 8478bf468e30a7c682b31a303a493fcc54b6fb9a
Author: Felipe Leme <felipeal@google.com>
Date:   Mon Apr 18 14:55:15 2016 -0700

    Added extra validation for invalid paths.

    In normal circumstances, StorageVolume can only be obtained through
    StorageManager and hence it will contain a valid path internally. But
    the path could be invalid in at least 2 occations:

    - App maliciously changed it using reflection.
    - Mounted volume was ejected.

    This change improves OpenExternalDirectoryActivity so it returns a
    RESULT_CANCELED upon receiveving an invalid path.

    BUG: 27962875

    Change-Id: Ide43968babaa37961c7b704bd289f44eac952e94