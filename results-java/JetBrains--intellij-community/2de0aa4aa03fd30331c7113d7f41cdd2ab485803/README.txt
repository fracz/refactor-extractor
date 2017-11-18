commit 2de0aa4aa03fd30331c7113d7f41cdd2ab485803
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Tue Oct 25 20:42:52 2016 +0300

    PY-14036: Support remote Django (and other) project creation
    * See PyProjectSynchronizer for entry point
    * DownloadAction refactored to extract  download
    * VagrantSupportImpl refactored to fetch mapped folders