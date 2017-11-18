commit 0dbe181428da146c340a312c68aabfb2e4611c08
Author: Eric Butler <eric@codebutler.com>
Date:   Fri May 3 18:29:47 2013 -0700

    Multipart request improvements.

    * Added fileName() to TypedOutput so non-File parts can be named.
    * Support Content-Length in multipart requests.
    * Serialize parts to byte[]s so length is available.