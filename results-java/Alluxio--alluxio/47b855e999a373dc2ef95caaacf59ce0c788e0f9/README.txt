commit 47b855e999a373dc2ef95caaacf59ce0c788e0f9
Author: jinntrance <jinntrance@gmail.com>
Date:   Mon Oct 21 19:34:22 2013 +0800

    CommonUtils: replaced if to switch statement among line 186-198 to make the code more legible.
    MasterInfo: corrected mWorkerAddressToId.remove() to remove the key instead of the value.
    UnderFileSystemHdfs: replaced the manual array copy with system library function to improve the performance