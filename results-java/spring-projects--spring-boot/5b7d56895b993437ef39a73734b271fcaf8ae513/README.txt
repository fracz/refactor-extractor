commit 5b7d56895b993437ef39a73734b271fcaf8ae513
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Tue Aug 27 01:05:43 2013 -0700

    Provide accurate InputStream.available() results

    Provide accurate InputStream.available() results by using the size
    attribute of the ZipEntry. This helps improve performance with
    CGLib and also fixes issues where libraries expect that a non-zero
    result from available() indicates that read() will not return -1.