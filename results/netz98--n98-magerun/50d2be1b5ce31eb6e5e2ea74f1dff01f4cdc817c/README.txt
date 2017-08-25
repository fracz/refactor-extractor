commit 50d2be1b5ce31eb6e5e2ea74f1dff01f4cdc817c
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Sun May 31 21:26:31 2015 +0200

    improved phar compression

    there are two different ways to compress a phar file: for each file inside
    the phar one after one or for all files at once.

    the all file at once method can trigger a runtime exception if not enough
    file handles for open files are available - but it is much faster.

    both variants have been extracted into a method of it's own and when the
    all files at once method fails, the file-by-file method is taken.

    the dead code has been removed as well.