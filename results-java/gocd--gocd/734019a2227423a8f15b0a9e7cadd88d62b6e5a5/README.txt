commit 734019a2227423a8f15b0a9e7cadd88d62b6e5a5
Author: Varsha Varadarajan <varshavaradarajan@users.noreply.github.com>
Date:   Fri Sep 23 14:18:34 2016 +0530

    Log improvements: (#2712)

    * Improved logs to differenciate secure environment variable errors when cipher is invalid, encrypted value is invalid and unknown errors.
    * Added logs in BackupService to provide info about how long each backup took.
    * Logging the exception that is caught while trying to create artifact zip file.