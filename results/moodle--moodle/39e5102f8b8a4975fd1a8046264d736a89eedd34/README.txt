commit 39e5102f8b8a4975fd1a8046264d736a89eedd34
Author: sam marshall <s.marshall@open.ac.uk>
Date:   Tue Sep 24 18:14:09 2013 +0100

    MDL-41838 Backup/restore: Support .tar.gz format for .mbz (2 of 2)

    The new experimental setting enabletgzbackups allows backups to be
    created so that the internal format for .mbz files is .tar.gz.

    Restore transparently supports .mbz files with either internal
    formats (.zip or .tar.gz).

    The .tar.gz format has the following benefits for backup:
    - Supports larger files (no limit on total size, 8GB on single file
      vs. 4GB limit on total size)
    - Compresses text better, resulting in smaller .mbz files.
    - Reports progress regularly during compression of single files,
      reducing the chance of timeouts during backups that include a
      very large file.

    Time performance may also be improved although I haven't done a
    direct comparison.