commit 11a76169b4f340122170b85cf32cd94b60d65251
Author: David Rodrigues <davrodpin@gmail.com>
Date:   Fri Apr 17 16:36:41 2015 -0700

    Overall improvement on Azure Deep Storage extension.

      * Remove hard-coded azure path manipulation from the puller.
      * Fix segment size not being zero after uploading it do Azure.
      * Remove both index and desc files only on a success upload to Azure.
      * Add Azure container name to load spec.
          This patch would help future-proof azure deep-storage module and avoid
          having to introduce ugly backwards-compatibility fixes when we want to
          support multiple containers or moving data between containers.