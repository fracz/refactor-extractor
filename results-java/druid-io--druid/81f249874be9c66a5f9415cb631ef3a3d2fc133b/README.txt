commit 81f249874be9c66a5f9415cb631ef3a3d2fc133b
Author: Goh Wei Xiang <xanec@users.noreply.github.com>
Date:   Thu Nov 9 17:38:19 2017 -0800

    Use daemon thread pool for AsyncHttpClient in emitters (#5057)

    * use daemon thread pool for AsyncHttpClient in emitters

    * changed to use existing helper methods

    * refactored creation of AsyncHttpClient